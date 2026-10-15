package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContentiousPlan;
import com.github.laxika.magicalvibes.cards.e.EbondeathDracolich;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntrepidOutlander;
import com.github.laxika.magicalvibes.cards.t.TargNar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        BardClass.class,
        ContentiousPlan.class,
        EbondeathDracolich.class,
        Forest.class,
        IntrepidOutlander.class,
        TargNar.class
})
class BardClassTest extends BaseCardTest {

    @Test
    void legendaryCreaturesEnterWithAdditionalCounter() {
        harness.castFromHand(player1, new BardClass(), "{R}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new EbondeathDracolich(), new IntrepidOutlander()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ebondeath, Dracolich").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(findPermanent(player1, "Intrepid Outlander").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void levelTwoReducesOnlyColoredManaOfLegendarySpells() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToTwo(bard);

        harness.setHand(player1, List.of(new TargNar()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Targ Nar, Demon-Fang Gnoll")).isNotNull();
    }

    @Test
    void levelTwoDoesNotReduceNonlegendarySpells() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToTwo(bard);

        harness.setHand(player1, List.of(new IntrepidOutlander()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelThreeExilesTopCardsWhenCastingLegendarySpell() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToThree(bard);

        Card first = new Forest();
        Card second = new IntrepidOutlander();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TargNar()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    void unusedColoredReductionDoesNotPayGenericOrOtherColors() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToTwo(bard);
        harness.setHand(player1, List.of(new EbondeathDracolich()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ebondeath, Dracolich");
    }

    @Test
    void levelThreeAllowsPlayingLandAndCastingSpellWithNormalCosts() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToThree(bard);
        Card land = new Forest();
        Card creature = new IntrepidOutlander();
        harness.setLibrary(player1, List.of(land, creature));
        harness.castFromHand(player1, new TargNar(), "{0}");
        resolveAllTriggers();

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Intrepid Outlander");
    }

    @Test
    void levelThreeTriggerSurvivesSourceLeavingBattlefield() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToThree(bard);
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new TargNar(), "{0}");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bard);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        harness.castFromExile(player1, card.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void legendarySpellAtLevelTwoDoesNotExileCards() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToTwo(bard);
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new TargNar(), "{0}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotSkipLevelTwoOrLevelUpOutsideMainPhase() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bard), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bard), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entryCounterDoesNotApplyToOpponentAndMultipleClassesStack() {
        harness.addToBattlefield(player1, new BardClass());
        harness.addToBattlefield(player1, new BardClass());
        harness.castFromHand(player1, new TargNar(), "{R}{G}");
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Targ Nar, Demon-Fang Gnoll")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TargNar(), "{R}{G}");
        resolveAllTriggers();
        assertThat(findPermanent(player2, "Targ Nar, Demon-Fang Gnoll")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonlegendarySpellAtLevelThreeDoesNotExileCards() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToThree(bard);
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new IntrepidOutlander(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exilePermissionExpiresAfterTheTurn() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToThree(bard);
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.castFromHand(player1, new TargNar(), "{0}");
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelTwoReductionDoesNotApplyToOpponent() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToTwo(bard);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TargNar()));

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Targ Nar, Demon-Fang Gnoll");
    }

    @Test
    void levelOneDoesNotReduceLegendarySpells() {
        harness.addToBattlefield(player1, new BardClass());
        harness.setHand(player1, List.of(new TargNar()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Targ Nar, Demon-Fang Gnoll");
    }

    @Test
    void classLevelDoesNotMakeBardEligibleForProliferate() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardClass());
        levelUpToTwo(bard);
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new ContentiousPlan(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    private void levelUpToTwo(Permanent bard) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(bard), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent bard) {
        levelUpToTwo(bard);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(bard), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
