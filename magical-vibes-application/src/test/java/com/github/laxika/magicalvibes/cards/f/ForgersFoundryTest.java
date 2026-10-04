package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlueSunsZenith;
import com.github.laxika.magicalvibes.cards.h.HighTide;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.StrokeOfGenius;
import com.github.laxika.magicalvibes.cards.t.Tidings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgersFoundry.class, HighTide.class, Tidings.class, IntoTheRoil.class, StrokeOfGenius.class, BlueSunsZenith.class})
class ForgersFoundryTest extends BaseCardTest {

    @Test
    void exilesEligibleSpellWhenAccepted() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        HighTide highTide = new HighTide();
        harness.setHand(player1, List.of(highTide));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).contains(highTide);
        harness.assertNotInGraveyard(player1, "High Tide");
    }

    @Test
    void doesNotTriggerForSpellWithManaValueAboveThree() {
        harness.addToBattlefield(player1, new ForgersFoundry());
        harness.setHand(player1, List.of(new Tidings()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tidings");
    }

    @Test
    void secondAbilityOffersCastingDuringResolution() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        HighTide highTide = new HighTide();
        gd.addToExile(player1.getId(), highTide, foundry.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.exileCastPermissionsUntilEndOfTurn).isEmpty();
    }

    @Test
    void declinedExileLeavesSpellInGraveyard() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        harness.setHand(player1, List.of(new HighTide()));
        harness.activateAbility(player1, 0, 0, null, null);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "High Tide");
        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).isEmpty();
    }

    @Test
    void ordinaryManaDoesNotTriggerExile() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());

        harness.castFromHand(player1, new HighTide(), "{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "High Tide");
        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).isEmpty();
    }

    @Test
    void manaStillTriggersAfterFoundryLeavesBattlefield() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        HighTide highTide = new HighTide();
        harness.setHand(player1, List.of(highTide));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, foundry.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forger's Foundry");

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).contains(highTide);
        harness.assertNotInGraveyard(player1, "High Tide");
    }

    @Test
    void chosenXCountsTowardManaValueLimit() {
        harness.addToBattlefield(player1, new ForgersFoundry());
        harness.setHand(player1, List.of(new StrokeOfGenius()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 1, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Stroke of Genius");
    }

    @Test
    void zeroXSpellAtManaValueThreeCanBeExiled() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        StrokeOfGenius stroke = new StrokeOfGenius();
        harness.setHand(player1, List.of(stroke));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).contains(stroke);
        harness.assertNotInGraveyard(player1, "Stroke of Genius");
    }

    @Test
    void secondAbilityWithNoExiledCardsResolvesWithoutChoice() {
        harness.addToBattlefield(player1, new ForgersFoundry());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selfShufflingSpellIsNotExiledInsteadOfShuffling() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        BlueSunsZenith zenith = new BlueSunsZenith();
        harness.setHand(player1, List.of(zenith));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(zenith);
        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).doesNotContain(zenith);
        harness.assertNotInGraveyard(player1, "Blue Sun's Zenith");
    }
}
