package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PicturesOfSpiderMan.class, Forest.class, GrizzlyBears.class})
class PicturesOfSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by putting up to two creature cards from the top five into hand")
    void entersAndPutsUpToTwoCreaturesIntoHand() {
        Card firstCreature = new GrizzlyBears();
        Card nonCreature = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondNonCreature = new Forest();
        Card thirdNonCreature = new Forest();
        harness.setLibrary(player1, List.of(firstCreature, nonCreature, secondCreature,
                secondNonCreature, thirdNonCreature));
        harness.setHand(player1, List.of(new PicturesOfSpiderMan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                nonCreature, secondNonCreature, thirdNonCreature);
    }

    @Test
    @DisplayName("Can decline to reveal a creature and put all five cards on the bottom")
    void canDeclineCreatureSelection() {
        Card firstCreature = new GrizzlyBears();
        Card nonCreature = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondNonCreature = new Forest();
        Card thirdNonCreature = new Forest();
        harness.setLibrary(player1, List.of(firstCreature, nonCreature, secondCreature,
                secondNonCreature, thirdNonCreature));
        harness.setHand(player1, List.of(new PicturesOfSpiderMan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                firstCreature, nonCreature, secondCreature, secondNonCreature, thirdNonCreature);
    }

    @Test
    @DisplayName("Sacrificing it creates a Treasure token")
    void sacrificeAbilityCreatesTreasure() {
        Permanent pictures = harness.addToBattlefieldAndReturn(player1, new PicturesOfSpiderMan());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pictures);
        harness.assertInGraveyard(player1, "Pictures of Spider-Man");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
    }
}
