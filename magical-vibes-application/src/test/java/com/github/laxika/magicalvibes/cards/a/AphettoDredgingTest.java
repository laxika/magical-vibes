package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiledriver;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoDredging.class, GoblinPiledriver.class, AirdropCondor.class,
        Shock.class, WoodlandChangeling.class})
class AphettoDredgingTest extends BaseCardTest {

    @Test
    void choosesUpToThreeCreaturesOfTheChosenType() {
        Card goblin = new GoblinPiledriver();
        Card bird = new AirdropCondor();
        Card changeling = new WoodlandChangeling();
        Card nonCreature = new Shock();
        harness.setGraveyard(player1, List.of(goblin, bird, changeling, nonCreature));
        harness.setHand(player1, List.of(new AphettoDredging()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithChosenCreatureType(player1, 0, 0, CardSubtype.GOBLIN, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(goblin.getId(), changeling.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId(), changeling.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goblin Piledriver");
        harness.assertInHand(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Airdrop Condor");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void noMatchingCardsPutsTheSpellOnTheStackWithoutAPrompt() {
        harness.setGraveyard(player1, List.of(new AirdropCondor()));
        harness.setHand(player1, List.of(new AphettoDredging()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithChosenCreatureType(player1, 0, 0, CardSubtype.GOBLIN, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Airdrop Condor");
    }

    @Test
    void doesNotReturnMoreThanThreeMatchingCreatures() {
        List<Card> goblins = List.of(
                new GoblinPiledriver(), new GoblinPiledriver(),
                new GoblinPiledriver(), new GoblinPiledriver());
        Card dredging = new AphettoDredging();
        harness.setGraveyard(player1, goblins);
        harness.setHand(player1, List.of(dredging));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithChosenCreatureType(player1, 0, 0, CardSubtype.GOBLIN, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                goblins.stream().map(Card::getId).toList());
        assertThat(choice.maxCount()).isEqualTo(3);

        List<Card> returned = goblins.subList(0, 3);
        harness.handleMultipleCardsChosen(player1, returned.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(returned.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(goblins.get(3).getId(), dredging.getId());
    }

    @Test
    void onlyOffersCardsFromTheCastersGraveyard() {
        Card ownGoblin = new GoblinPiledriver();
        Card opponentGoblin = new GoblinPiledriver();
        harness.setGraveyard(player1, List.of(ownGoblin));
        harness.setGraveyard(player2, List.of(opponentGoblin));
        harness.setHand(player1, List.of(new AphettoDredging()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithChosenCreatureType(player1, 0, 0, CardSubtype.GOBLIN, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownGoblin.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownGoblin.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goblin Piledriver");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentGoblin.getId());
    }
}
