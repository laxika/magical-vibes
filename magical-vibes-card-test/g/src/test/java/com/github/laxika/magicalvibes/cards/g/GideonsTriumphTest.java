package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelCelebrant;
import com.github.laxika.magicalvibes.cards.c.CentaurNurturer;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonsTriumph.class, GideonBlackblade.class, PollenbrightDruid.class, CentaurNurturer.class,
        CruelCelebrant.class})
class GideonsTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent chooses one creature that attacked or blocked this turn")
    void opponentChoosesOneAttackedOrBlockedCreature() {
        Permanent attacker = addCreatureReady(player2, new PollenbrightDruid());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CentaurNurturer());
        blocker.setBlocking(true);
        Permanent untouched = addCreatureReady(player2, new PollenbrightDruid());

        castTriumph(player2);

        harness.handleMultiplePermanentsChosen(player2, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker, untouched);
        harness.assertInGraveyard(player2, "Pollenbright Druid");
    }

    @Test
    @DisplayName("A Gideon planeswalker makes the opponent sacrifice two eligible creatures")
    void gideonMakesOpponentSacrificeTwo() {
        addGideon(player1);
        Permanent attacker = addCreatureReady(player2, new PollenbrightDruid());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CentaurNurturer());
        blocker.setBlocking(true);
        Permanent untouched = addCreatureReady(player2, new PollenbrightDruid());

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker, blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untouched);
        harness.assertInGraveyard(player2, "Pollenbright Druid");
        harness.assertInGraveyard(player2, "Centaur Nurturer");
    }

    @Test
    @DisplayName("The Gideon condition is checked as the spell resolves")
    void gideonConditionIsCheckedOnResolution() {
        Permanent attacker = addCreatureReady(player2, new PollenbrightDruid());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CentaurNurturer());
        blocker.setBlocking(true);

        harness.setHand(player1, List.of(new GideonsTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player2.getId());
        addGideon(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker, blocker);
    }

    @Test
    @DisplayName("Creatures that did not attack or block this turn are not eligible")
    void untouchedCreatureIsNotEligible() {
        Permanent untouched = addCreatureReady(player2, new PollenbrightDruid());

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untouched);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gideon's Triumph can target only an opponent")
    void onlyTargetsOpponent() {
        harness.setHand(player1, List.of(new GideonsTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures remain eligible after they stop attacking or blocking")
    void creaturesRemainEligibleAfterCombat() {
        Permanent attacker = addCreatureReady(player2, new PollenbrightDruid());
        attacker.setAttacking(true);
        attacker.setAttacking(false);
        Permanent blocker = addCreatureReady(player2, new CentaurNurturer());
        blocker.setBlocking(true);
        blocker.setBlocking(false);
        addGideon(player1);

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker, blocker);
    }

    @Test
    @DisplayName("With Gideon, the opponent sacrifices their only eligible creature")
    void gideonWithOnlyOneEligibleCreature() {
        addGideon(player1);
        Permanent attacker = addCreatureReady(player2, new PollenbrightDruid());
        attacker.setAttacking(true);
        Permanent untouched = addCreatureReady(player2, new CentaurNurturer());

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untouched).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Pollenbright Druid");
    }

    @Test
    @DisplayName("An opponent's Gideon does not increase the sacrifice count")
    void opponentsGideonDoesNotUpgradeSpell() {
        addGideon(player2);
        Permanent attacker = addCreatureReady(player2, new PollenbrightDruid());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CentaurNurturer());
        blocker.setBlocking(true);

        castTriumph(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of(blocker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker).doesNotContain(blocker);
    }

    @Test
    @DisplayName("With Gideon, the opponent chooses two of three eligible creatures")
    void opponentChoosesTwoEligibleCreatures() {
        addGideon(player1);
        Permanent first = addCreatureReady(player2, new PollenbrightDruid());
        first.setAttacking(true);
        Permanent second = addCreatureReady(player2, new CentaurNurturer());
        second.setBlocking(true);
        Permanent third = addCreatureReady(player2, new PollenbrightDruid());
        third.setAttacking(true);

        castTriumph(player2);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first, third);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Two eligible creatures are sacrificed simultaneously, so each sees both deaths")
    void automaticSacrificesAreSimultaneous() {
        addGideon(player1);
        Permanent first = addCreatureReady(player2, new CruelCelebrant());
        first.setAttacking(true);
        Permanent second = addCreatureReady(player2, new CruelCelebrant());
        second.setAttacking(true);

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }

    private void castTriumph(Player targetPlayer) {
        harness.setHand(player1, List.of(new GideonsTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }

    private void addGideon(Player player) {
        Permanent gideon = harness.addToBattlefieldAndReturn(player, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
    }
}
