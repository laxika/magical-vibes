package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({BarkformHarvester.class, HillGiant.class})
class BarkformHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from its graveyard on the bottom of its library")
    void putsTargetCardOnBottomOfLibrary() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        Card target = new HillGiant();
        Card existingLibraryCard = new HillGiant();
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
        Card target = new HillGiant();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, harvesterIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
