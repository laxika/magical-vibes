package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarkformHarvester.class, FountainportBell.class})
class BarkformHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from its graveyard on the bottom of its library")
    void putsTargetCardOnBottomOfLibrary() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Card target = new BarkformHarvester();
        Card existingLibraryCard = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(existingLibraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        harness.activateAbilityWithGraveyardTargets(player1, harvesterIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingLibraryCard, target);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Card target = new BarkformHarvester();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, harvesterIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can put a noncreature card into an empty library")
    void returnsNoncreatureCardToEmptyLibrary() {
        harness.addToBattlefield(player1, new BarkformHarvester());
        Card target = new FountainportBell();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Can activate twice immediately without tapping, preserving resolution order")
    void canActivateTwiceWithoutTapping() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Card first = new BarkformHarvester();
        Card second = new FountainportBell();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(first.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harvester.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Cannot activate with only one mana")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new BarkformHarvester());
        Card target = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("A second activation on the same card makes the first target illegal")
    void doesNotMoveTargetAgainAfterItLeavesGraveyard() {
        harness.addToBattlefield(player1, new BarkformHarvester());
        Card target = new FountainportBell();
        Card remaining = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(target, remaining));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }
}
