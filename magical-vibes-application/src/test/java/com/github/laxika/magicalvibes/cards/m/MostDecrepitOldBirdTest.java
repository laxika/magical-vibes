package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpeakSecrets;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MostDecrepitOldBird.class, SpeakSecrets.class, Shock.class, GrizzlyBears.class})
class MostDecrepitOldBirdTest extends BaseCardTest {

    @Test
    void adventureMillsFourAndReturnsAnInstantOrSorceryFromAmongThem() {
        MostDecrepitOldBird card = new MostDecrepitOldBird();
        Shock instant = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(instant, creature, new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(instant));

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCreatesNoChoiceWhenNoInstantOrSorceryWasMilled() {
        MostDecrepitOldBird card = new MostDecrepitOldBird();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void getsPlusOnePlusOneAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new MostDecrepitOldBird());

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    @Test
    void remainsOneOneBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new MostDecrepitOldBird());

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
