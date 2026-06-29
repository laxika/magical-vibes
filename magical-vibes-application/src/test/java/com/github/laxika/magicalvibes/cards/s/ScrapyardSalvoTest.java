package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapyardSalvoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to target player equal to artifact cards in controller's graveyard")
    void dealsDamageEqualToArtifactCardsInGraveyard() {
        // Put 3 artifact cards in player1's graveyard
        Card artifact1 = new com.github.laxika.magicalvibes.cards.s.Spellbook();
        Card artifact2 = new com.github.laxika.magicalvibes.cards.s.Spellbook();
        Card artifact3 = new com.github.laxika.magicalvibes.cards.s.Spellbook();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(artifact1, artifact2, artifact3));

        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17); // 20 - 3
    }

    @Test
    @DisplayName("Deals zero damage when no artifact cards in graveyard")
    void dealsZeroDamageWithNoArtifacts() {
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count non-artifact cards in graveyard")
    void doesNotCountNonArtifactCards() {
        // Put a creature (non-artifact) and an artifact in the graveyard
        Card creature = new com.github.laxika.magicalvibes.cards.l.LlanowarElves();
        Card artifact = new com.github.laxika.magicalvibes.cards.s.Spellbook();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(creature, artifact));

        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only 1 artifact card, so 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // 20 - 1
    }

    @Test
    @DisplayName("Counts artifact creature cards in graveyard")
    void countsArtifactCreatureCards() {
        // Put an artifact creature in the graveyard (has ARTIFACT as additional type)
        Card artifactCreature = new com.github.laxika.magicalvibes.cards.m.MyrSuperion();
        Card artifact = new com.github.laxika.magicalvibes.cards.s.Spellbook();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(artifactCreature, artifact));

        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 artifact cards (artifact creature + pure artifact)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // 20 - 2
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new ScrapyardSalvo()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Scrapyard Salvo");
    }
}
