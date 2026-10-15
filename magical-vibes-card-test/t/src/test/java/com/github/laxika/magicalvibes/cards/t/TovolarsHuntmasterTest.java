package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TovolarsHuntmaster.class, TovolarsPackleader.class, DawnhartRejuvenator.class})
class TovolarsHuntmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two 2/2 green Wolf tokens")
    void entersWithTwoWolves() {
        harness.setHand(player1, List.of(new TovolarsHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
        assertThat(findPermanents(player1, "Wolf")).allMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.WOLF));
    }

    @Test
    @DisplayName("Transforms into Tovolar's Packleader when the previous active player cast no spells")
    void transformsWhenNoSpellsWereCast() {
        Permanent huntmaster = harness.enterBattlefieldAndReturn(player1, new TovolarsHuntmaster());
        resolveAllTriggers();
        gd.spellsCastLastTurn.clear();
        gd.previousTurnActivePlayerId = player2.getId();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(huntmaster.isTransformed()).isTrue();
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    @DisplayName("Transforms back when the previous active player cast two or more spells")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent packleader = addTransformedPackleader(player1);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        gd.previousTurnActivePlayerId = player2.getId();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(packleader.isTransformed()).isFalse();
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    @DisplayName("Creates two Wolves whenever the Packleader attacks")
    void attackingCreatesTwoWolves() {
        addTransformedPackleader(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    @DisplayName("Another Wolf or Werewolf fights a creature the controller does not control")
    void anotherWolfOrWerewolfFightsOpponentCreature() {
        Permanent packleader = addTransformedPackleader(player1);
        Permanent otherWerewolf = addTransformedPackleader(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(otherWerewolf.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(otherWerewolf.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(packleader.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The Packleader cannot target itself as the other Wolf or Werewolf")
    void cannotTargetItselfAsFirstTarget() {
        Permanent packleader = addTransformedPackleader(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(packleader.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersTransformedAtNightAndCreatesTwoWolves() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new TovolarsHuntmaster()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent packleader = findPermanent(player1, "Tovolar's Packleader");
        assertThat(packleader.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    void nonactivePlayersSpellsDoNotPreventNight() {
        gd.dayNight = DayNight.DAY;
        Permanent huntmaster = addCreatureReady(player1, new TovolarsHuntmaster());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(huntmaster.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void nonactivePlayersTwoSpellsDoNotCreateAnUpkeepAbility() {
        Permanent packleader = addTransformedPackleader(player1);
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(packleader.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oneSpellKeepsTheHuntmasterFrontFaceUp() {
        gd.dayNight = DayNight.DAY;
        Permanent huntmaster = addCreatureReady(player1, new TovolarsHuntmaster());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(huntmaster.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void aWolfTokenCanFightWithoutTappingThePackleader() {
        gd.dayNight = DayNight.NIGHT;
        Permanent packleader = harness.enterBattlefieldAndReturn(player1, new TovolarsHuntmaster());
        resolveAllTriggers();
        Permanent wolf = findPermanents(player1, "Wolf").getFirst();
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(wolf.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wolf);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(packleader.isTapped()).isFalse();
        assertThat(packleader.getMarkedDamage()).isZero();
    }

    @Test
    void noFightOccursIfOneTargetLeavesBeforeResolution() {
        addTransformedPackleader(player1);
        Permanent otherWerewolf = addTransformedPackleader(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(otherWerewolf.getId(), opponentCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature.getCard());

        harness.passBothPriorities();

        assertThat(otherWerewolf.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherWerewolf);
    }

    @Test
    void fightStillResolvesIfThePackleaderLeaves() {
        Permanent packleader = addTransformedPackleader(player1);
        Permanent otherWerewolf = addTransformedPackleader(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(otherWerewolf.getId(), opponentCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(packleader);
        gd.playerGraveyards.get(player1.getId()).add(packleader.getOriginalCard());

        harness.passBothPriorities();

        assertThat(otherWerewolf.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    void cannotUseACreatureWithoutWolfOrWerewolfSubtype() {
        addTransformedPackleader(player1);
        Permanent ownCreature = addCreatureReady(player1, new DawnhartRejuvenator());
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(ownCreature.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseAnOpponentsWerewolfAsTheFirstTarget() {
        addTransformedPackleader(player1);
        Permanent opponentWerewolf = addTransformedPackleader(player2);
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(opponentWerewolf.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotFightACreatureYouControl() {
        addTransformedPackleader(player1);
        Permanent otherWerewolf = addTransformedPackleader(player1);
        Permanent ownCreature = addCreatureReady(player1, new DawnhartRejuvenator());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(otherWerewolf.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedPackleader(Player player) {
        gd.dayNight = DayNight.NIGHT;
        Permanent packleader = addCreatureReady(player, new TovolarsHuntmaster());
        packleader.setCard(packleader.getOriginalCard().getBackFaceCard());
        packleader.setTransformed(true);
        return packleader;
    }
}
