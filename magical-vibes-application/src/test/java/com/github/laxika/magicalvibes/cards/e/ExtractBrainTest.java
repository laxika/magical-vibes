package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtractBrain.class, Divination.class, Forest.class, GrizzlyBears.class})
class ExtractBrainTest extends BaseCardTest {

    @Test
    @DisplayName("The target chooses X cards and the caster may cast a revealed spell for free")
    void targetChoosesCardsAndCasterCastsRevealedSpell() {
        Card land = new Forest();
        Card spell = new Divination();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(land, spell, creature)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        PendingInteraction.RevealCardsDiscardChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(castChoice).isNotNull();
        assertThat(castChoice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(castChoice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).singleElement().extracting(entry -> entry.getCard().getId())
                .isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
    }

    @Test
    @DisplayName("Lands among the chosen cards are not offered as spells")
    void landsAreNotOffered() {
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(land)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.stack).isEmpty();
    }
}
