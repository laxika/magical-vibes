package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VedalkenMesmerist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MnemonicBetrayal.class, Forest.class, Shock.class, VedalkenMesmerist.class})
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

    @Test
    void returnsAllRemainingCardsWithOneDelayedTrigger() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setGraveyard(player2, List.of(first, second));
        castMnemonicBetrayal();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void casterControlsTheDelayedReturnTrigger() {
        harness.setGraveyard(player2, List.of(new Forest()));
        castMnemonicBetrayal();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    void castCardThatExilesItselfDoesNotReturnAtEndStep() {
        MnemonicBetrayal borrowed = new MnemonicBetrayal();
        harness.setGraveyard(player2, List.of(borrowed));
        castMnemonicBetrayal();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, borrowed.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(borrowed);
        assertThatThrownBy(() -> harness.castFromExile(player1, borrowed.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");

        advanceToEndStep();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(borrowed);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(borrowed);
    }

    @Test
    void creatureRemainsUnderCastersControlAndOwnGraveyardIsUnaffected() {
        VedalkenMesmerist creature = new VedalkenMesmerist();
        Forest ownCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(creature));
        castMnemonicBetrayal();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Vedalken Mesmerist");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void permissionDoesNotOverrideCreatureTiming() {
        VedalkenMesmerist creature = new VedalkenMesmerist();
        harness.setGraveyard(player2, List.of(creature));
        castMnemonicBetrayal();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
    }

    @Test
    void emptyOpponentGraveyardStillExilesBetrayal() {
        harness.setGraveyard(player2, List.of());
        MnemonicBetrayal betrayal = castMnemonicBetrayal();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(betrayal);
        harness.assertNotInGraveyard(player1, "Mnemonic Betrayal");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isEmpty();
    }

    private MnemonicBetrayal castMnemonicBetrayal() {
        MnemonicBetrayal betrayal = new MnemonicBetrayal();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(betrayal));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        return betrayal;
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();
    }
}
