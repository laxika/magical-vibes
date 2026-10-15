package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Microscope.class, DarksteelCitadel.class})
class MicroscopeTest extends BaseCardTest {

    @Test
    void surveilsOne() {
        addReadyMicroscope();
        Card topCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void animatesTargetPermanentCardInAnyGraveyardUntilEndOfTurn() {
        addReadyMicroscope();
        Card target = new DarksteelCitadel();
        harness.setGraveyard(player2, List.of(target));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gqs.cardHasType(target, CardType.CREATURE, gd, player2.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(target, CardSubtype.GERM, gd, player2.getId())).isTrue();
        assertThat(gqs.getEffectiveCardColors(gd, target)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveCardPower(gd, target)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, target)).isZero();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gqs.cardHasType(target, CardType.CREATURE, gd, player2.getId())).isFalse();
        assertThat(gqs.cardHasSubtype(target, CardSubtype.GERM, gd, player2.getId())).isFalse();
        assertThat(gqs.getEffectiveCardColors(gd, target)).isEmpty();
    }

    @Test
    void surveilAbilityTapsMicroscopeAsItsCost() {
        Permanent microscope = addReadyMicroscope();
        harness.setLibrary(player1, List.of(new DarksteelCitadel()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(microscope.isTapped()).isTrue();
    }

    @Test
    void animationAbilityTapsMicroscopeAsItsCost() {
        Permanent microscope = addReadyMicroscope();
        Card target = new DarksteelCitadel();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);

        assertThat(microscope.isTapped()).isTrue();
    }

    @Test
    void tappedMicroscopeCannotSurveil() {
        Permanent microscope = addReadyMicroscope();
        microscope.tap();
        harness.setLibrary(player1, List.of(new DarksteelCitadel()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedMicroscopeCannotAnimateAGraveyardCard() {
        Permanent microscope = addReadyMicroscope();
        microscope.tap();
        Card target = new DarksteelCitadel();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canKeepSurveilledCardOnTopWithoutDrawingIt() {
        addReadyMicroscope();
        Card topCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void animationPreservesPermanentTypesAndDoesNotMoveCardFromOwnGraveyard() {
        addReadyMicroscope();
        Card target = new DarksteelCitadel();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gqs.cardHasType(target, CardType.CREATURE, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(target, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(target, CardType.LAND, gd, player1.getId())).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    private Permanent addReadyMicroscope() {
        Permanent microscope = harness.addToBattlefieldAndReturn(player1, new Microscope());
        microscope.setSummoningSick(false);
        return microscope;
    }
}
