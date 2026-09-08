package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeroOfTheDunesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a qualifying artifact from the graveyard to the battlefield")
    void returnsQualifyingArtifact() {
        Card artifact = new CharcoalDiamond();
        Card expensiveCreature = new AirElemental();
        harness.setGraveyard(player1, List.of(artifact, expensiveCreature));
        castHero();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hero of the Dunes");
        harness.assertOnBattlefield(player1, "Charcoal Diamond");
        harness.assertNotInGraveyard(player1, "Charcoal Diamond");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Does not offer a card with mana value greater than three")
    void doesNotOfferHighManaValueCard() {
        Card creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        castHero();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Boosts only your creatures with mana value three or less")
    void boostsOnlyCheapOwnCreatures() {
        Permanent cheapCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent expensiveCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroOfTheDunes());

        assertThat(gqs.getEffectivePower(gd, cheapCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, expensiveCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    private void castHero() {
        harness.setHand(player1, List.of(new HeroOfTheDunes()));
        addHeroMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addHeroMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
