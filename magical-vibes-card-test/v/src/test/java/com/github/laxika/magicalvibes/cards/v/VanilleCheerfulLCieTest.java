package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FangFearlessLCie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGigas;
import com.github.laxika.magicalvibes.cards.r.RagnarokDivineDeliverance;
import com.github.laxika.magicalvibes.cards.r.RelmsSketching;
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

@CardUsed({VanilleCheerfulLCie.class, RagnarokDivineDeliverance.class, FangFearlessLCie.class,
        Forest.class, HillGigas.class, RelmsSketching.class})
class VanilleCheerfulLCieTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling two cards and returning a permanent card to hand")
    void millsAndReturnsPermanentToHand() {
        Forest milledFirst = new Forest();
        Forest milledSecond = new Forest();
        HillGigas returned = new HillGigas();
        harness.setLibrary(player1, List.of(milledFirst, milledSecond));
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new VanilleCheerfulLCie()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(returned));

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledFirst, milledSecond);
    }

    @Test
    @DisplayName("Melds with Fang after paying {3}{B}{G}")
    void meldsWithFang() {
        harness.setLibrary(player1, List.of());
        Permanent vanille = harness.addToBattlefieldAndReturn(player1, new VanilleCheerfulLCie());
        Permanent fang = harness.addToBattlefieldAndReturn(player1, new FangFearlessLCie());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent == vanille || permanent == fang);
        Permanent ragnarok = findPermanent(player1, "Ragnarok, Divine Deliverance");
        assertThat(ragnarok.getMeldComponentCards()).hasSize(2);
        assertThat(ragnarok.isTapped()).isFalse();
    }

    @Test
    void ragnarokDeathAbilityUsesOneStackEntryForBothTargets() {
        Forest returned = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        Permanent ragnarok = harness.addToBattlefieldAndReturn(player1, new RagnarokDivineDeliverance());
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new FangFearlessLCie());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragnarok));
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, destroyed.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Fang, Fearless l'Cie");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void tokenCopyOfFangIsExiledButCannotMeld() {
        harness.addToBattlefield(player1, new VanilleCheerfulLCie());
        Permanent originalFang = harness.addToBattlefieldAndReturn(player2, new FangFearlessLCie());
        harness.setHand(player1, List.of(new RelmsSketching()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, originalFang.getId());
        harness.assertOnBattlefield(player1, "Fang, Fearless l'Cie");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ragnarok, Divine Deliverance");
        harness.assertNotOnBattlefield(player1, "Vanille, Cheerful l'Cie");
        harness.assertNotOnBattlefield(player1, "Fang, Fearless l'Cie");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof VanilleCheerfulLCie);
    }

    @Test
    void decliningPaymentLeavesBothMeldComponentsOnBattlefield() {
        harness.addToBattlefield(player1, new VanilleCheerfulLCie());
        harness.addToBattlefield(player1, new FangFearlessLCie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Vanille, Cheerful l'Cie");
        harness.assertOnBattlefield(player1, "Fang, Fearless l'Cie");
        harness.assertNotOnBattlefield(player1, "Ragnarok, Divine Deliverance");
    }

    @Test
    @DisplayName("On death, destroys a permanent and returns a nonlegendary permanent card")
    void deathAbilityDestroysAndReturns() {
        Forest returned = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        Permanent ragnarok = harness.addToBattlefieldAndReturn(player1, new RagnarokDivineDeliverance());
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new HillGigas());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragnarok));
        harness.runStateBasedActions();
        assertThat(gd.pendingInteractions).isNotEmpty();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, destroyed.getId());
        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice).isNotNull();
        assertThat(graveyardChoice.validCardIds()).containsExactly(returned.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Gigas");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void canReturnAPermanentThatWasJustMilled() {
        Forest returned = new Forest();
        RelmsSketching nonpermanent = new RelmsSketching();
        harness.setLibrary(player1, List.of(returned, nonpermanent));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new VanilleCheerfulLCie()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(
                gd.playerGraveyards.get(player1.getId()).indexOf(returned));
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(returned));

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonpermanent);
    }

    @Test
    void stillReturnsAPermanentWhenLibraryHasFewerThanTwoCards() {
        HillGigas returned = new HillGigas();
        RelmsSketching milled = new RelmsSketching();
        harness.setLibrary(player1, List.of(milled));
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new VanilleCheerfulLCie()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(returned));

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
    }

    @Test
    void finishesWithoutAChoiceWhenNoPermanentCanBeReturned() {
        RelmsSketching first = new RelmsSketching();
        RelmsSketching second = new RelmsSketching();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new VanilleCheerfulLCie()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        harness.assertOnBattlefield(player1, "Vanille, Cheerful l'Cie");
    }

    @Test
    void doesNotTriggerWithoutFang() {
        harness.addToBattlefield(player1, new VanilleCheerfulLCie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerWhenVanilleIsOwnedByOpponent() {
        VanilleCheerfulLCie vanille = new VanilleCheerfulLCie();
        vanille.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, vanille);
        harness.addToBattlefield(player1, new FangFearlessLCie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerWhenFangIsOwnedByOpponent() {
        FangFearlessLCie fang = new FangFearlessLCie();
        fang.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, new VanilleCheerfulLCie());
        harness.addToBattlefield(player1, fang);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    void cannotPayWhenFangLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new VanilleCheerfulLCie());
        Permanent fang = harness.addToBattlefieldAndReturn(player1, new FangFearlessLCie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, fang));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Vanille, Cheerful l'Cie");
        harness.assertInHand(player1, "Fang, Fearless l'Cie");
        harness.assertNotOnBattlefield(player1, "Ragnarok, Divine Deliverance");
    }

    @Test
    void tokenCopyOfVanilleAndRealFangAreExiledWithoutMelding() {
        Permanent originalVanille = harness.addToBattlefieldAndReturn(player2, new VanilleCheerfulLCie());
        harness.addToBattlefield(player1, new FangFearlessLCie());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new RelmsSketching()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, originalVanille.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Vanille, Cheerful l'Cie");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Vanille, Cheerful l'Cie");
        harness.assertNotOnBattlefield(player1, "Fang, Fearless l'Cie");
        harness.assertNotOnBattlefield(player1, "Ragnarok, Divine Deliverance");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof FangFearlessLCie);
        harness.assertOnBattlefield(player2, "Vanille, Cheerful l'Cie");
    }

    @Test
    void meldedRagnarokHasCombinedManaValueAndDiesAsBothComponents() {
        VanilleCheerfulLCie vanille = new VanilleCheerfulLCie();
        FangFearlessLCie fang = new FangFearlessLCie();
        harness.addToBattlefield(player1, vanille);
        harness.addToBattlefield(player1, fang);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent ragnarok = findPermanent(player1, "Ragnarok, Divine Deliverance");
        assertThat(gqs.getPermanentManaValue(ragnarok)).isEqualTo(7);

        Forest returned = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragnarok));
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, destroyed.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(vanille, fang);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Ragnarok, Divine Deliverance");
    }
}
