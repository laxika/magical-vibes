package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.d.DoggedDetective;
import com.github.laxika.magicalvibes.cards.m.MarshalingCry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YixlidJailer.class, AvenInitiate.class, MarshalingCry.class, DakmorSalvage.class,
        DoggedDetective.class})
class YixlidJailerTest extends BaseCardTest {

    @Test
    void preventsActivatingAbilitiesOfCardsInGraveyards() {
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new AvenInitiate()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }

    @Test
    void preventsFlashbackFromGraveyards() {
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventsDredgeReplacementFromGraveyards() {
        DakmorSalvage salvage = new DakmorSalvage();
        MarshalingCry drawn = new MarshalingCry();
        MarshalingCry remaining = new MarshalingCry();
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(salvage));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, remaining));

        drawCard(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(salvage);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void preventsGraveyardTriggeredAbilitiesFromFiring() {
        DoggedDetective detective = new DoggedDetective();
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player2, List.of(new MarshalingCry(), new MarshalingCry()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(detective);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
