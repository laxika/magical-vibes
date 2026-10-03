package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CartographersCompanion.class, Plains.class})
class CartographersCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Map token")
    void etbCreatesMapToken() {
        harness.setHand(player1, List.of(new CartographersCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).singleElement()
                .satisfies(map -> assertThat(map.getCard().hasType(CardType.ARTIFACT)).isTrue())
                .satisfies(map -> assertThat(map.getCard().getSubtypes()).contains(CardSubtype.MAP));
    }

    @Test
    void mapExploringLandPutsItInHandWithoutCounter() {
        Permanent companion = castCompanion();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land));

        activateMap(companion);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(companion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void mapExploringNonlandCanLeaveItOnTop() {
        Permanent companion = castCompanion();
        CartographersCompanion revealed = new CartographersCompanion();
        harness.setLibrary(player1, List.of(revealed));

        activateMap(companion);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(companion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void mapExploringNonlandCanPutItInGraveyard() {
        Permanent companion = castCompanion();
        CartographersCompanion revealed = new CartographersCompanion();
        harness.setLibrary(player1, List.of(revealed));

        activateMap(companion);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(companion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    void mapExploringEmptyLibraryStillAddsCounter() {
        Permanent companion = castCompanion();
        harness.setLibrary(player1, List.of());

        activateMap(companion);
        resolveAllTriggers();

        assertThat(companion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void mapCannotTargetOpponentsCreature() {
        castCompanion();
        harness.addToBattlefield(player2, new CartographersCompanion());
        Permanent opponent = findPermanent(player2, "Cartographer's Companion");

        assertThatThrownBy(() -> activateMap(opponent)).isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    void mapCannotActivateDuringCombat() {
        Permanent companion = castCompanion();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> activateMap(companion)).isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    private Permanent castCompanion() {
        harness.setHand(player1, List.of(new CartographersCompanion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Cartographer's Companion");
    }

    private void activateMap(Permanent target) {
        Permanent map = findPermanent(player1, "Map");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(map),
                null, target.getId());
    }
}
