package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PicturesOfSpiderMan.class, Forest.class, GrizzlyBears.class, LurkingLizards.class})
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
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
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
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
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

    @Test
    void canChooseOneCreatureAndOnlyMovesTheTopFiveCards() {
        Card first = new LurkingLizards();
        Card second = new LurkingLizards();
        Card third = new LurkingLizards();
        Card artifact = new PicturesOfSpiderMan();
        Card otherArtifact = new PicturesOfSpiderMan();
        Card sixth = new LurkingLizards();
        Card seventh = new PicturesOfSpiderMan();
        harness.setLibrary(player1, List.of(first, second, third, artifact, otherArtifact, sixth, seventh));
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(sixth.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6).startsWith(sixth, seventh);
        assertThat(deck.subList(2, 6)).containsExactlyInAnyOrder(first, third, artifact, otherArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canDeclineTheOnlyCreatureInAShortLibrary() {
        Card creature = new LurkingLizards();
        Card artifact = new PicturesOfSpiderMan();
        harness.setLibrary(player1, List.of(creature, artifact));
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTakeBothCreaturesFromAShortLibrary() {
        Card first = new LurkingLizards();
        Card second = new LurkingLizards();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noCreaturesMovesAllLookedAtCardsBelowTheUntouchedCards() {
        Card first = new PicturesOfSpiderMan();
        Card second = new PicturesOfSpiderMan();
        Card third = new PicturesOfSpiderMan();
        Card fourth = new PicturesOfSpiderMan();
        Card fifth = new PicturesOfSpiderMan();
        Card sixth = new LurkingLizards();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(6).startsWith(sixth);
        assertThat(deck.subList(1, 6)).containsExactlyInAnyOrder(first, second, third, fourth, fifth);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNull();
    }

    @Test
    void emptyLibraryDoesNotPreventTheArtifactFromEntering() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new PicturesOfSpiderMan(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pictures of Spider-Man");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedArtifactCannotPayTheActivationCost() {
        Permanent pictures = harness.addToBattlefieldAndReturn(player1, new PicturesOfSpiderMan());
        pictures.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pictures);
        harness.assertNotInGraveyard(player1, "Pictures of Spider-Man");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresOneManaAndCreatesTreasureOnlyOnResolution() {
        Permanent pictures = harness.addToBattlefieldAndReturn(player1, new PicturesOfSpiderMan());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pictures);
        harness.assertNotInGraveyard(player1, "Pictures of Spider-Man");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pictures);
        harness.assertInGraveyard(player1, "Pictures of Spider-Man");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }
}
