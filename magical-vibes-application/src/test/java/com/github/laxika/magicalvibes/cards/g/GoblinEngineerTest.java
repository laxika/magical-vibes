package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinEngineer.class, BraidwoodCup.class, SolemnSimulacrum.class, Spellbook.class})
class GoblinEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB ability may put an artifact from the library into the graveyard")
    void searchesArtifactIntoGraveyard() {
        Spellbook artifact = new Spellbook();
        setUpAndCast(artifact);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(artifact);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The activated ability sacrifices an artifact and returns a qualifying artifact")
    void returnsArtifactWithManaValueThreeOrLess() {
        Permanent engineer = addReadyEngineer();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        Spellbook target = new Spellbook();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(engineer.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sacrifice.getCard().getId()));
    }

    @Test
    @DisplayName("The activated ability cannot target an artifact with mana value greater than three")
    void rejectsArtifactAboveManaValueThree() {
        addReadyEngineer();
        harness.addToBattlefield(player1, new BraidwoodCup());
        Card target = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEngineer() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new GoblinEngineer());
        engineer.setSummoningSick(false);
        return engineer;
    }

    private void setUpAndCast(Card... libraryCards) {
        harness.setHand(player1, List.of(new GoblinEngineer()));
        harness.setLibrary(player1, List.of(libraryCards));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
