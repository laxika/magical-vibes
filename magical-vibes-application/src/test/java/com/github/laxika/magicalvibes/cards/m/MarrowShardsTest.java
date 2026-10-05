package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlindZealot;
import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarrowShards.class, BlindZealot.class, GlistenerElf.class})
class MarrowShardsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature but does not kill 2-toughness attackers")
    void dealsOneToEachAttacker() {
        addAttacker(player2, new BlindZealot());
        addAttacker(player2, new BlindZealot());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        // One damage leaves both 2-toughness attackers alive.
        long attackerCount = countPermanents(player2, "Blind Zealot");
        assertThat(attackerCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        Card oneOne = new GlistenerElf();
        addAttacker(player2, oneOne);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Glistener Elf");
        harness.assertInGraveyard(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        // Use 1/1 creatures â€” if they were damaged they'd die
        Card oneOne1 = new GlistenerElf();
        Card oneOne2 = new GlistenerElf();
        harness.addToBattlefield(player1, oneOne1);
        harness.addToBattlefield(player2, oneOne2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        // Both non-attacking 1/1 creatures survive since they were not damaged
        harness.assertOnBattlefield(player1, "Glistener Elf");
        harness.assertOnBattlefield(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker(player2, new BlindZealot());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Also damages the caster's own attacking creatures")
    void damagesCastersAttackers() {
        addAttacker(player1, new GlistenerElf());
        harness.addToBattlefield(player2, new GlistenerElf());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Glistener Elf");
        harness.assertOnBattlefield(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("Can pay two life instead of white mana")
    void canPayLife() {
        addAttacker(player2, new GlistenerElf());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MarrowShards()));

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("Only creatures still attacking at resolution are damaged")
    void checksAttackingStatusAtResolution() {
        Permanent removedFromCombat = addAttacker(player2, new GlistenerElf());
        addAttacker(player2, new GlistenerElf());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        removedFromCombat.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(removedFromCombat);
        harness.assertInGraveyard(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("Damage prevention can save an attacking creature")
    void respectsDamagePrevention() {
        Permanent attacker = addAttacker(player2, new GlistenerElf());
        attacker.setDamagePreventionShield(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MarrowShards()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Glistener Elf");
        assertThat(attacker.getDamagePreventionShield()).isZero();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }

}
