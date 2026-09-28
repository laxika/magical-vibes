package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SteelfinWhale.class, Spellbook.class, GrizzlyBears.class})
class SteelfinWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new SteelfinWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new SteelfinWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An artifact you control entering untaps Steelfin Whale")
    void ownArtifactEntryUntapsSteelfinWhale() {
        Permanent whale = addCreatureReady(player1, new SteelfinWhale());
        whale.tap();

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(whale.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact entering does not untap Steelfin Whale")
    void opponentArtifactEntryDoesNotUntapSteelfinWhale() {
        Permanent whale = addCreatureReady(player1, new SteelfinWhale());
        whale.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(whale.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A nonartifact entering does not untap Steelfin Whale")
    void nonartifactEntryDoesNotUntapSteelfinWhale() {
        Permanent whale = addCreatureReady(player1, new SteelfinWhale());
        whale.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(whale.isTapped()).isTrue();
    }
}
