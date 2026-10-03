package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IfritWardenOfInferno;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CliveIfritsDominant.class, IfritWardenOfInferno.class, GrizzlyBears.class})
class CliveIfritsDominantTest extends BaseCardTest {

    @Test
    void mayDiscardHandAndDrawRedDevotionOnEntry() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new CliveIfritsDominant(), new GrizzlyBears()));
        addCliveMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void activatedAbilityTransformsAndChapterOneFightsAnotherCreature() {
        addCreatureReady(player1, new CliveIfritsDominant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCliveMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent ifrit = findPermanent(player1, "Ifrit, Warden of Inferno");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(ifrit.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(ifrit.isTransformed()).isTrue();
        assertThat(ifrit.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void chapterOneMayDeclineItsOptionalTarget() {
        addCreatureReady(player1, new CliveIfritsDominant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCliveMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void chapterTwoAddsFourRedMana() {
        Permanent ifrit = addIfritWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(ifrit.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void chapterThreeAddsManaThenReturnsIfritToItsFrontFace() {
        addIfritWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent clive = findPermanent(player1, "Clive, Ifrit's Dominant");
        assertThat(clive.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof IfritWardenOfInferno);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void mayDeclineDiscardWithoutDrawing() {
        GrizzlyBears heldCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new CliveIfritsDominant(), heldCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCliveMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(heldCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayDrawForDevotionWithAnEmptyHand() {
        harness.setHand(player1, List.of(new CliveIfritsDominant()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCliveMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void transformedIfritCannotAttackOnTheTurnItReturns() {
        addCreatureReady(player1, new CliveIfritsDominant());
        addCliveMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();

        Permanent ifrit = findPermanent(player1, "Ifrit, Warden of Inferno");
        assertThat(harness.getAttackLegalityService().canAttack(gd, ifrit, player1.getId())).isFalse();
    }

    @Test
    void returnedCliveCannotTransformAgainBeforeItsControllersNextTurn() {
        addIfritWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCliveMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void chapterTwoChecksLoreCountWhenItResolves() {
        Permanent ifrit = addIfritWithLore(1);

        advanceToNextChapter();
        ifrit.setCounterCount(CounterType.LORE, 3);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(findPermanent(player1, "Clive, Ifrit's Dominant").isTransformed()).isFalse();
    }

    private void addCliveMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private Permanent addIfritWithLore(int loreCounters) {
        CliveIfritsDominant front = new CliveIfritsDominant();
        Permanent ifrit = new Permanent(front);
        ifrit.setCard(front.getBackFaceCard());
        ifrit.setTransformed(true);
        ifrit.setSummoningSick(false);
        ifrit.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(ifrit);
        return ifrit;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
