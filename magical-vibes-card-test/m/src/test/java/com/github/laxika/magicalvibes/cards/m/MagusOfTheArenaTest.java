package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheArena.class, GiantDustwasp.class, MireBoa.class})
class MagusOfTheArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent chooses the second creature, which is tapped and fights the first")
    void opponentChoosesSecondCreatureAndItFights() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new MireBoa());
        Permanent opposingDustwasp = addCreatureReady(player2, new GiantDustwasp());
        Permanent opposingBoa = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(opposingDustwasp.getId(), opposingBoa.getId());

        harness.handlePermanentChosen(player2, opposingDustwasp.getId());
        harness.passBothPriorities();

        assertThat(magus.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mire Boa");
        assertThat(opposingDustwasp.isTapped()).isTrue();
        assertThat(opposingDustwasp.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingBoa.isTapped()).isFalse();
        assertThat(opposingBoa.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Taps the remaining target but does not fight when one target is gone")
    void tapsRemainingTargetWhenOpponentTargetIsGone() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new GiantDustwasp());
        Permanent boa = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());
        harness.handlePermanentChosen(player2, boa.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(magus.isTapped()).isTrue();
        assertThat(fighter.isTapped()).isTrue();
        assertThat(fighter.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Already tapped creatures still fight and both deal lethal damage")
    void alreadyTappedCreaturesStillFight() {
        addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new MireBoa());
        Permanent opponent = addCreatureReady(player2, new MireBoa());
        fighter.tap();
        opponent.tap();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());
        harness.handlePermanentChosen(player2, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mire Boa");
        harness.assertInGraveyard(player2, "Mire Boa");
        harness.assertNotOnBattlefield(player1, "Mire Boa");
        harness.assertNotOnBattlefield(player2, "Mire Boa");
    }

    @Test
    @DisplayName("Magus can target itself despite tapping to pay its activation cost")
    void magusCanFightItselfAsTheControllerTarget() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheArena());
        Permanent opponent = addCreatureReady(player2, new GiantDustwasp());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, magus.getId());
        harness.handlePermanentChosen(player2, opponent.getId());
        assertThat(magus.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Magus of the Arena");
        assertThat(magus.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Giant Dustwasp");
    }

    @Test
    @DisplayName("Taps the opponent's creature without fighting when the first target is gone")
    void tapsRemainingTargetWhenControllerTargetIsGone() {
        addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new MireBoa());
        Permanent opponent = addCreatureReady(player2, new GiantDustwasp());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());
        harness.handlePermanentChosen(player2, opponent.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fighter);
        harness.passBothPriorities();

        assertThat(opponent.isTapped()).isTrue();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Giant Dustwasp");
    }

    @Test
    @DisplayName("The chosen creatures still fight after Magus leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new GiantDustwasp());
        Permanent opponent = addCreatureReady(player2, new MireBoa());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());
        harness.handlePermanentChosen(player2, opponent.getId());
        gd.playerBattlefields.get(player1.getId()).remove(magus);
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isTrue();
        assertThat(fighter.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Mire Boa");
    }

    @Test
    @DisplayName("A creature no longer controlled by the activator is neither tapped nor fought")
    void controllerTargetBecomesIllegalAfterControlChange() {
        addCreatureReady(player1, new MagusOfTheArena());
        Permanent fighter = addCreatureReady(player1, new MireBoa());
        Permanent opponent = addCreatureReady(player2, new GiantDustwasp());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, fighter.getId());
        harness.handlePermanentChosen(player2, opponent.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fighter);
        gd.playerBattlefields.get(player2.getId()).add(fighter);
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isFalse();
        assertThat(fighter.getMarkedDamage()).isZero();
        assertThat(opponent.isTapped()).isTrue();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Mire Boa");
    }
}
