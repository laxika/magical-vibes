package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.ZephyrSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquaticSubtlety.class, GrizzlyBears.class, Mulldrifter.class, Unsummon.class, ZephyrSprite.class})
class AquaticSubtletyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, bottoms two, and perpetually grants Evoke to blue creature cards left in hand")
    void drawsBottomsAndGrantsEvoke() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        GrizzlyBears remainingNonblue = new GrizzlyBears();
        ZephyrSprite firstBlue = new ZephyrSprite();
        ZephyrSprite secondBlue = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, remainingNonblue));
        harness.setLibrary(player1, List.of(firstBlue, secondBlue));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstBottom, secondBottom, remainingNonblue, firstBlue, secondBlue);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remainingNonblue, firstBlue, secondBlue);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstBottom, secondBottom);

        assertThatThrownBy(() -> harness.getGameService().playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blue creature can be cast for granted Evoke by exiling another blue card")
    void castsBlueCreatureWithGrantedEvoke() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        GrizzlyBears remainingNonblue = new GrizzlyBears();
        ZephyrSprite evoked = new ZephyrSprite();
        ZephyrSprite payment = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, remainingNonblue));
        harness.setLibrary(player1, List.of(evoked, payment));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.inMutationScope(() -> harness.getSpellCastingService().playCardWithAlternateCost(
                gd, player1, 1, 0, null, null, List.of(), 2));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zephyr Sprite");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
    }

    @Test
    void grantedEvokeIsPlayableWithoutManaWhenBluePaymentIsAvailable() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        ZephyrSprite evoked = new ZephyrSprite();
        AquaticSubtlety payment = new AquaticSubtlety();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, evoked, payment));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));
        harness.ensurePriority(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(harness.getGameActionAvailabilityService().getPlayableCardIndices(gd, player1.getId()))
                .contains(0);
    }

    @Test
    void grantedEvokeCanBeChosenInsteadOfNativeEvoke() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        Mulldrifter evoked = new Mulldrifter();
        AquaticSubtlety payment = new AquaticSubtlety();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, evoked, payment));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));
        harness.ensurePriority(player1);
        harness.inMutationScope(() -> harness.getSpellCastingService().playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of(), 1));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
        harness.assertInGraveyard(player1, "Mulldrifter");
        harness.assertNotOnBattlefield(player1, "Mulldrifter");
    }

    @Test
    void blueNoncreatureCanPayForEvokeButDoesNotGainIt() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        AquaticSubtlety payment = new AquaticSubtlety();
        ZephyrSprite evoked = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, payment, evoked));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.inMutationScope(() ->
                harness.getSpellCastingService().playCardWithAlternateCost(
                        gd, player1, 0, 0, null, null, List.of(), 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.inMutationScope(() -> harness.getSpellCastingService().playCardWithAlternateCost(
                gd, player1, 1, 0, null, null, List.of(), 0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
        harness.assertInGraveyard(player1, "Zephyr Sprite");
    }

    @Test
    void normalCastingDoesNotSacrificeCreatureWithGrantedEvoke() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        ZephyrSprite creature = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, creature));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zephyr Sprite");
        harness.assertNotInGraveyard(player1, "Zephyr Sprite");
    }

    @Test
    void bottomedBlueCreatureDoesNotGainEvoke() {
        ZephyrSprite bottomed = new ZephyrSprite();
        GrizzlyBears otherBottom = new GrizzlyBears();
        AquaticSubtlety payment = new AquaticSubtlety();
        harness.setHand(player1, List.of(new AquaticSubtlety(), bottomed, otherBottom, payment));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(otherBottom.getId(), bottomed.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherBottom, bottomed);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.inMutationScope(() ->
                harness.getSpellCastingService().playCardWithAlternateCost(
                        gd, player1, 4, 0, null, null, List.of(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void evokeCannotExileItselfOrANonblueCard() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        GrizzlyBears nonblue = new GrizzlyBears();
        ZephyrSprite evoked = new ZephyrSprite();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom, evoked, nonblue));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.inMutationScope(() ->
                harness.getSpellCastingService().playCardWithAlternateCost(
                        gd, player1, 0, 0, null, null, List.of(), 0)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.inMutationScope(() ->
                harness.getSpellCastingService().playCardWithAlternateCost(
                        gd, player1, 0, 0, null, null, List.of(), 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).contains(evoked, nonblue);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void grantedEvokePersistsAfterBattlefieldAndReturnToHand() {
        GrizzlyBears firstBottom = new GrizzlyBears();
        GrizzlyBears secondBottom = new GrizzlyBears();
        ZephyrSprite creature = new ZephyrSprite();
        AquaticSubtlety payment = new AquaticSubtlety();
        harness.setHand(player1, List.of(new AquaticSubtlety(), firstBottom, secondBottom,
                creature, new Unsummon(), payment));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstBottom.getId(), secondBottom.getId()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Zephyr Sprite"));
        harness.assertInHand(player1, "Zephyr Sprite");
        harness.ensurePriority(player1);
        harness.inMutationScope(() -> harness.getSpellCastingService().playCardWithAlternateCost(
                gd, player1, 3, 0, null, null, List.of(), 0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(payment);
        harness.assertInGraveyard(player1, "Zephyr Sprite");
        harness.assertNotOnBattlefield(player1, "Zephyr Sprite");
    }
}
