package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepurposingBayTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactAndPutsAnArtifactWithManaValueOneHigherOntoTheBattlefield() {
        harness.addToBattlefield(player1, new RepurposingBay());
        Ornithopter ornithopter = new Ornithopter();
        harness.addToBattlefield(player1, ornithopter);
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(spellbomb));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Ornithopter");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == spellbomb);
    }

    @Test
    void offersOnlyArtifactCardsWithTheMatchingManaValue() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        AetherSpellbomb spellbomb = new AetherSpellbomb();
        harness.setLibrary(player1, List.of(spellbomb, new MindStone(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(spellbomb);
    }

    @Test
    void cannotActivateWithoutAnotherArtifact() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void canOnlyBeActivatedAtSorcerySpeed() {
        harness.addToBattlefield(player1, new RepurposingBay());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
