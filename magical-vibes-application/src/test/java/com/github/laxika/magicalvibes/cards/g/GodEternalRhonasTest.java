package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodEternalRhonas.class, GiantGrowth.class, GrizzlyBears.class, Forest.class,
        Mountain.class, Plains.class, SwordsToPlowshares.class, WrathOfGod.class})
class GodEternalRhonasTest extends BaseCardTest {

    @Test
    @DisplayName("ETB doubles the power of other creatures and grants them vigilance")
    void enterTriggerDoublesOtherOwnCreaturePowerAndGrantsVigilance() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GodEternalRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rhonas = findPermanent(player1, "God-Eternal Rhonas");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, rhonas)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, rhonas, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB doubles each creature's current power without changing its toughness")
    void enterTriggerUsesEachCreatureCurrentPower() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.setHand(player1, List.of(new GodEternalRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB power doubling and vigilance wear off at end of turn")
    void enterTriggerEffectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GodEternalRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The death trigger may put God-Eternal Rhonas third from the top")
    void deathTriggerPutsRhonasThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new GodEternalRhonas());
        Card rhonas = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        destroyRhonasWithWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), rhonas.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(rhonas.getId()));
    }

    @Test
    @DisplayName("Declining the death trigger leaves God-Eternal Rhonas in the graveyard")
    void decliningDeathTriggerLeavesRhonasInGraveyard() {
        harness.addToBattlefield(player1, new GodEternalRhonas());
        Card rhonas = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        destroyRhonasWithWrath();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(rhonas.getId()));
    }

    @Test
    @DisplayName("The exile trigger may put God-Eternal Rhonas third from the top")
    void exileTriggerPutsRhonasThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        Permanent rhonasPermanent = harness.addToBattlefieldAndReturn(player1, new GodEternalRhonas());
        Card rhonas = rhonasPermanent.getCard();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, rhonasPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), rhonas.getId(), third.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(rhonas.getId()));
    }

    @Test
    @DisplayName("Creatures entering after the ETB resolves receive neither bonus")
    void laterCreaturesReceiveNeitherBonus() {
        harness.setHand(player1, List.of(new GodEternalRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A pump resolving after the doubling adds its normal bonus")
    void laterPumpIsNotDoubled() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GodEternalRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("The death trigger puts Rhonas on the bottom of a short library")
    void deathTriggerUsesBottomOfShortLibrary() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        Permanent rhonas = harness.addToBattlefieldAndReturn(player1, new GodEternalRhonas());

        destroyRhonasWithWrath();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), rhonas.getCard().getId());
        harness.assertNotInGraveyard(player1, "God-Eternal Rhonas");
    }

    @Test
    @DisplayName("Declining the exile trigger leaves Rhonas in exile")
    void decliningExileTriggerLeavesRhonasInExile() {
        Permanent rhonas = harness.addToBattlefieldAndReturn(player1, new GodEternalRhonas());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, rhonas.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rhonas.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(rhonas.getCard().getId()));
    }

    @Test
    @DisplayName("The ETB trigger still affects other creatures after Rhonas leaves")
    void enterTriggerResolvesAfterRhonasLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GodEternalRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent rhonas = findPermanent(player1, "God-Eternal Rhonas");

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, rhonas.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        harness.assertNotOnBattlefield(player1, "God-Eternal Rhonas");
    }

    @Test
    @DisplayName("The exile trigger can return Rhonas to an empty library")
    void exileTriggerUsesEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent rhonas = harness.addToBattlefieldAndReturn(player1, new GodEternalRhonas());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, rhonas.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(rhonas.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(rhonas.getCard().getId()));
    }

    private void destroyRhonasWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
