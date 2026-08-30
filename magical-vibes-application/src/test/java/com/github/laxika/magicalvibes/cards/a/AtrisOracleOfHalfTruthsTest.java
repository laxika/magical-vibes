package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtrisOracleOfHalfTruths.class, Forest.class, Island.class, Plains.class, Swamp.class})
class AtrisOracleOfHalfTruthsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB asks the targeted opponent to separate the top three cards")
    void targetedOpponentSeparatesTopThree() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains));

        cast();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                island.getId(), forest.getId(), swamp.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.peekPendingInteraction(PendingPileSeparation.class).targetPlayerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The controller chooses either pile for hand and puts the other into the graveyard")
    void controllerChoosesPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(island, forest, swamp));

        cast();
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.description()).contains(forest.getName(), swamp.getName())
                .doesNotContain(island.getName());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(forest, swamp);
    }

    @Test
    @DisplayName("The ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new AtrisOracleOfHalfTruths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void cast() {
        harness.setHand(player1, List.of(new AtrisOracleOfHalfTruths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
