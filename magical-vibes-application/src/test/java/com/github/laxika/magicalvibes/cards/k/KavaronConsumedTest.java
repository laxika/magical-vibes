package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavaronConsumed.class, GrizzlyBears.class, SolRing.class})
class KavaronConsumedTest extends BaseCardTest {

    @Test
    @DisplayName("Offers artifact and creature cards in hand")
    void offersArtifactAndCreatureCards() {
        harness.setHand(player1, List.of(new KavaronConsumed(), new GrizzlyBears(), new SolRing()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 1);
    }

    @Test
    @DisplayName("Perpetually makes the chosen card an artifact creature and sacrifices it at the next end step")
    void changesAndSacrificesChosenCard() {
        harness.setHand(player1, List.of(new KavaronConsumed(), new SolRing()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent chosen = findPermanent(player1, "Sol Ring");
        assertThat(gqs.isArtifact(gd, chosen)).isTrue();
        assertThat(gqs.isCreature(gd, chosen)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(4);
        assertThat(chosen.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Sol Ring");
        Card graveyardCard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Sol Ring"))
                .findFirst()
                .orElseThrow();
        assertThat(graveyardCard.getType()).isEqualTo(CardType.CREATURE);
        assertThat(graveyardCard.getAdditionalTypes()).containsExactly(CardType.ARTIFACT);
        assertThat(graveyardCard.getPower()).isEqualTo(4);
        assertThat(graveyardCard.getToughness()).isEqualTo(4);
        assertThat(graveyardCard.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
