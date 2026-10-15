package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CallToHeel;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisceraDragger.class, ResoundingThunder.class, CallToHeel.class})
@DisplayName("Viscera Dragger")
class VisceraDraggerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new VisceraDragger()));
        harness.setLibrary(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Viscera Dragger");
        harness.assertInHand(player1, "Resounding Thunder");
    }

    @Test
    @DisplayName("Unearth returns Viscera Dragger to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Viscera Dragger");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Viscera Dragger");
    }

    @Test
    @DisplayName("Unearthed Viscera Dragger is exiled at the next end step")
    void unearthExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Viscera Dragger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Viscera Dragger"));
    }

    @Test
    void cyclingDiscardsAsACostBeforeDrawing() {
        VisceraDragger card = new VisceraDragger();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        harness.assertNotInHand(player1, "Viscera Dragger");
        harness.assertNotInHand(player1, "Resounding Thunder");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Resounding Thunder");
    }

    @Test
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Viscera Dragger");
        harness.assertNotInGraveyard(player1, "Viscera Dragger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingCanBeActivatedDuringOpponentsUpkeep() {
        harness.setHand(player2, List.of(new VisceraDragger()));
        harness.setLibrary(player2, List.of(new ResoundingThunder()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Viscera Dragger");
        harness.assertInHand(player2, "Resounding Thunder");
    }

    @Test
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Viscera Dragger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthRequiresOwnTurn() {
        harness.setGraveyard(player2, List.of(new VisceraDragger()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Viscera Dragger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Viscera Dragger");
    }

    @Test
    void unearthRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Viscera Dragger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthRequiresGenericManaInAdditionToBlack() {
        harness.setGraveyard(player1, List.of(new VisceraDragger()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Viscera Dragger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalDamageExilesUnearthedDragger() {
        VisceraDragger card = new VisceraDragger();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Viscera Dragger").getId());

        harness.assertNotOnBattlefield(player1, "Viscera Dragger");
        harness.assertNotInGraveyard(player1, "Viscera Dragger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void returningUnearthedDraggerToHandExilesItAndStillDraws() {
        VisceraDragger card = new VisceraDragger();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new CallToHeel()));
        harness.setLibrary(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Viscera Dragger").getId());

        harness.assertNotOnBattlefield(player1, "Viscera Dragger");
        harness.assertNotInHand(player1, "Viscera Dragger");
        harness.assertNotInGraveyard(player1, "Viscera Dragger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertInHand(player1, "Resounding Thunder");
    }
}
