package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RazorfinHunter;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulLink.class, RazorfinHunter.class, YavimayaCoast.class})
class SoulLinkTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to combat damage dealt by the enchanted creature")
    void gainsLifeFromCombatDamageDealtByEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("You gain life equal to noncombat damage dealt by the enchanted creature")
    void gainsLifeFromNoncombatDamageDealtByEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("You gain life equal to damage dealt to the enchanted creature")
    void gainsLifeFromDamageDealtToEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new RazorfinHunter());
        Permanent damageSource = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(damageSource),
                null, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both abilities trigger when the enchanted creature deals damage to itself")
    void bothAbilitiesTriggerWhenEnchantedCreatureDealsDamageToItself() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                null, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new RazorfinHunter());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.setHand(player1, List.of(new SoulLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opponent's enchanted creature gains life for the Aura controller after damage")
    void opponentCreatureDamageGainsLifeForAuraControllerOnResolution() {
        Permanent creature = addCreatureReady(player2, new RazorfinHunter());
        castSoulLink(creature);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.ensurePriority(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(creature),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both abilities resolve after enchanted creature and Aura die in combat")
    void bothCombatDamageAbilitiesSurviveLethalDamage() {
        Permanent attacker = addCreatureReady(player1, new RazorfinHunter());
        Permanent blocker = addCreatureReady(player2, new RazorfinHunter());
        castSoulLink(attacker);
        harness.setLife(player1, 10);

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertInGraveyard(player1, "Razorfin Hunter");
        harness.assertInGraveyard(player1, "Soul Link");
        harness.assertInGraveyard(player2, "Razorfin Hunter");
    }

    @Test
    @DisplayName("Each Soul Link independently triggers for damage from the enchanted creature")
    void multipleSoulLinksEachGainLife() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);
        castSoulLink(creature);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 19);
    }

    private void castSoulLink(Permanent creature) {
        harness.setHand(player1, List.of(new SoulLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
