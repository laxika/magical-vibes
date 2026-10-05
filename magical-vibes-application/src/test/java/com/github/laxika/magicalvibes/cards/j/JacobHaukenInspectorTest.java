package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StormchaserDrake;
import com.github.laxika.magicalvibes.cards.t.ThirstForDiscovery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacobHaukenInspector.class, HaukensInsight.class, StormchaserDrake.class,
        ThirstForDiscovery.class, Island.class})
class JacobHaukenInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability draws then exiles a card from hand face down")
    void drawsThenExilesCardFromHandFaceDown() {
        Permanent inspector = addInspectorReady();
        CardPair cards = setUpLibraryAndHand();

        activateInspector(inspector);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(cards.exiled().getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isEqualTo(inspector.getId());
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cards.drawn());
        assertThat(inspector.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Activated ability transforms after the controller pays")
    void transformsAfterPaying() {
        Permanent inspector = addInspectorReady();
        setUpLibraryAndHand();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateInspector(inspector);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(inspector.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Hauken's Insight exiles the top card and allows one free cast each turn")
    void backFaceExilesTopCardAndAllowsOneFreeCastEachTurn() {
        Permanent insight = addTransformedInsight();
        Card topCard = new StormchaserDrake();
        Card secondCard = new StormchaserDrake();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.addToExile(player1.getId(), secondCard, insight.getId(), true);

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isEqualTo(insight.getId());
        assertThat(entry.faceDown()).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, secondCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playingExiledLandUsesTheSameAllowanceAsCastingASpell() {
        Permanent insight = addTransformedInsight();
        Card land = new Island();
        Card spell = new StormchaserDrake();
        gd.addToExile(player1.getId(), land, insight.getId(), true);
        gd.addToExile(player1.getId(), spell, insight.getId(), true);
        prepareMainPhase();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingExiledSpellPreventsPlayingAnExiledLandThatTurn() {
        Permanent insight = addTransformedInsight();
        Card spell = new StormchaserDrake();
        Card land = new Island();
        gd.addToExile(player1.getId(), spell, insight.getId(), true);
        gd.addToExile(player1.getId(), land, insight.getId(), true);
        prepareMainPhase();

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormchaser Drake");
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cardsExiledBeforeTransformingCanBeCastAfterTransforming() {
        Permanent inspector = addInspectorReady();
        CardPair cards = setUpLibraryAndHand();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        activateInspector(inspector);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        prepareMainPhase();

        harness.castFromExile(player1, cards.exiled().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormchaser Drake");
        assertThat(gd.findExiledCard(cards.exiled().getId())).isNull();
    }

    @Test
    void mayDeclineTransformEvenWithEnoughMana() {
        Permanent inspector = addInspectorReady();
        CardPair cards = setUpLibraryAndHand();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        activateInspector(inspector);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(inspector.isTransformed()).isFalse();
        assertThat(gd.findExiledCard(cards.exiled().getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cards.drawn());
    }

    @Test
    void abilityStillDrawsAndExilesAfterInspectorLeavesBattlefield() {
        Permanent inspector = addInspectorReady();
        CardPair cards = setUpLibraryAndHand();
        prepareMainPhase();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, inspector);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.findExiledCard(cards.exiled().getId())).isNotNull();
        assertThat(gd.findExiledCard(cards.exiled().getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cards.drawn());
    }

    @Test
    void insightCannotCastCardsDuringOpponentsTurn() {
        Permanent insight = addTransformedInsight();
        Card spell = new ThirstForDiscovery();
        gd.addToExile(player1.getId(), spell, insight.getId(), true);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void freeCreatureSpellStillRequiresNormalSorceryTiming() {
        Permanent insight = addTransformedInsight();
        Card spell = new StormchaserDrake();
        gd.addToExile(player1.getId(), spell, insight.getId(), true);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void upkeepWithEmptyLibraryDoesNotAttemptToDraw() {
        addTransformedInsight();
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    void insightLosesCastingPermissionWhenItLeavesBattlefield() {
        Permanent insight = addTransformedInsight();
        Card spell = new StormchaserDrake();
        gd.addToExile(player1.getId(), spell, insight.getId(), true);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, insight);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addInspectorReady() {
        return addCreatureReady(player1, new JacobHaukenInspector());
    }

    private Permanent addTransformedInsight() {
        JacobHaukenInspector card = new JacobHaukenInspector();
        Permanent insight = harness.addToBattlefieldAndReturn(player1, card);
        insight.setCard(card.getBackFaceCard());
        insight.setTransformed(true);
        insight.setSummoningSick(false);
        return insight;
    }

    private CardPair setUpLibraryAndHand() {
        Card drawn = new Island();
        Card exiled = new StormchaserDrake();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        return new CardPair(drawn, exiled);
    }

    private void activateInspector(Permanent inspector) {
        prepareMainPhase();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(inspector), 0, null, null);
        harness.passBothPriorities();
    }

    private record CardPair(Card drawn, Card exiled) {
    }
}
