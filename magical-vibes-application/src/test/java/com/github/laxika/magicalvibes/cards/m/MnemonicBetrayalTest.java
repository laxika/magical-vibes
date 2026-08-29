package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MnemonicBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Casts an opponent-owned spell with any mana and returns uncast cards at the next end step")
    void castsWithAnyManaAndReturnsUncastCards() {
        Shock shock = new Shock();
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(shock, forest));
        MnemonicBetrayal betrayal = castMnemonicBetrayal();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(shock, forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(betrayal);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);

        advanceToEndStep();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not allow a land exiled by Mnemonic Betrayal to be played")
    void doesNotAllowPlayingExiledLand() {
        Forest forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        castMnemonicBetrayal();

        assertThatThrownBy(() -> harness.castFromExile(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");

        advanceToEndStep();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private MnemonicBetrayal castMnemonicBetrayal() {
        MnemonicBetrayal betrayal = new MnemonicBetrayal();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(betrayal));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        return betrayal;
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
