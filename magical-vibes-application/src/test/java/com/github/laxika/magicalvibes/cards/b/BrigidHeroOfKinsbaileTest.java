package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrigidHeroOfKinsbaileTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature target player controls")
    void deals2DamageToEachAttackingCreature() {
        addBrigidReady(player1);
        addAttacker(player2, makeCreature("Sturdy Cadet", 1, 3));
        addAttacker(player2, makeCreature("Sturdy Cadet", 1, 3));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).hasSize(2);
        assertThat(battlefield).allMatch(p -> p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Deals 2 damage to each blocking creature target player controls")
    void deals2DamageToEachBlockingCreature() {
        addBrigidReady(player1);
        Permanent blocker = addBlockingCreature(player2, makeCreature("Sturdy Cadet", 1, 3));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kills 2-toughness attacking creatures")
    void killsTwoToughnessAttackers() {
        addBrigidReady(player1);
        addAttacker(player2, makeCreature("Eager Cadet", 1, 2));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Eager Cadet"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Eager Cadet"));
    }

    @Test
    @DisplayName("Does not damage non-attacking or blocking creatures of target player")
    void doesNotDamageNonCombatCreatures() {
        addBrigidReady(player1);
        harness.addToBattlefield(player2, makeCreature("Eager Cadet", 1, 2));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Eager Cadet") && p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("Does not damage attacking creatures controlled by other players")
    void doesNotDamageOtherPlayersAttackers() {
        addBrigidReady(player1);
        addAttacker(player1, makeCreature("Eager Cadet", 1, 2));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Eager Cadet") && p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("Activating ability taps Brigid")
    void activatingTapsBrigid() {
        Permanent brigid = addBrigidReady(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player2.getId());

        assertThat(brigid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target yourself even when you have no attacking or blocking creatures")
    void canTargetSelfWithNoEffect() {
        addBrigidReady(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

  // ===== Helpers =====

    private Permanent addBrigidReady(Player player) {
        BrigidHeroOfKinsbaile card = new BrigidHeroOfKinsbaile();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBlockingCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{W}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
