package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuzzioVisionaryArchitect.class, WornPowerstone.class, GildedLotus.class,
        Ornithopter.class, GrizzlyBears.class})
class MuzzioVisionaryArchitectTest extends BaseCardTest {

    @Test
    @DisplayName("Uses the greatest mana value among your artifacts and offers only artifacts in range")
    void usesControlledArtifactManaValueAndArtifactFilter() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player2, new GildedLotus());

        Card nonArtifact = new GrizzlyBears();
        Card firstArtifact = new Ornithopter();
        Card secondArtifact = new GildedLotus();
        Card unlookedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonArtifact, firstArtifact, secondArtifact, unlookedCard));
        activate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactly("Ornithopter", "Gilded Lotus");

        harness.handleCardChosen(player1, 1);
        harness.assertOnBattlefield(player1, "Gilded Lotus");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unlookedCard, firstArtifact, nonArtifact);
    }

    @Test
    @DisplayName("Declining leaves all looked-at cards on the bottom")
    void mayDeclineArtifactChoice() {
        addCreatureReady(player1, new MuzzioVisionaryArchitect());
        harness.addToBattlefield(player1, new WornPowerstone());

        Card firstArtifact = new Ornithopter();
        Card nonArtifact = new GrizzlyBears();
        Card secondArtifact = new GildedLotus();
        Card unlookedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstArtifact, nonArtifact, secondArtifact, unlookedCard));
        activate();

        harness.handleCardChosen(player1, -1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unlookedCard, secondArtifact, nonArtifact, firstArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firstArtifact.getId()));
    }

    @Test
    @DisplayName("An opponent's artifacts do not increase the number of cards looked at")
    void opponentArtifactsDoNotCount() {
        Permanent setup = setupMuzzioWithoutControlledArtifact();
        harness.addToBattlefield(player2, new GildedLotus());
        Card topCard = new Ornithopter();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        activate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(setup.isTapped()).isTrue();
    }

    private void activate() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent setupMuzzioWithoutControlledArtifact() {
        return addCreatureReady(player1, new MuzzioVisionaryArchitect());
    }
}
