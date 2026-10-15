package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MostDecrepitOldBird.class, SpeakSecrets.class, Shock.class, GrizzlyBears.class, CounselOfTheSoratami.class})
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

    @Test
    void adventureReturnsASorceryFromAShortLibrary() {
        MostDecrepitOldBird card = new MostDecrepitOldBird();
        CounselOfTheSoratami sorcery = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(sorcery, new MostDecrepitOldBird()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(sorcery));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotReturnAnOlderGraveyardCardOrDeclineTheChoice() {
        MostDecrepitOldBird card = new MostDecrepitOldBird();
        Shock older = new Shock();
        Shock milled = new Shock();
        harness.setGraveyard(player1, List.of(older));
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(milled, new MostDecrepitOldBird()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(milled));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(older);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureWithAnEmptyLibrary() {
        MostDecrepitOldBird card = new MostDecrepitOldBird();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Most Decrepit Old Bird");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void thresholdTracksOnlyItsControllersGraveyardAndUpdatesWhenCardsLeave() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new MostDecrepitOldBird());
        harness.setGraveyard(player2, graveyardCards(7));
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);

        harness.setGraveyard(player1, graveyardCards(8));
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);

        harness.setGraveyard(player1, graveyardCards(6));
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
