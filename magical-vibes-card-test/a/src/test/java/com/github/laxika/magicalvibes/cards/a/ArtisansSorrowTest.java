package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThassaGodOfTheSea;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtisansSorrow.class, AngelicChorus.class, FountainOfYouth.class,
        GrizzlyBears.class, ThassaGodOfTheSea.class})
class ArtisansSorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and then scries 2")
    void destroysArtifactAndScries() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.addToBattlefield(player2, artifact);
        prepare();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);
        Card originalSecond = deck.get(1);
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(originalTop, originalSecond);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(deck.get(deck.size() - 2)).isSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalSecond);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroys an enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castAndResolveInstant(player1, 0, targetId);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a non-artifact non-enchantment permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Does not scry when its only target leaves before resolution")
    void doesNotScryWithIllegalTarget() {
        FountainOfYouth artifact = new FountainOfYouth();
        harness.addToBattlefield(player2, artifact);
        prepare();
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        gd.playerGraveyards.get(player2.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalLibrary);
        harness.assertInGraveyard(player1, "Artisan's Sorrow");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scries even when an indestructible target cannot be destroyed")
    void scriesWithIndestructibleTarget() {
        harness.addToBattlefield(player2, new ThassaGodOfTheSea());
        prepare();
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Thassa, God of the Sea"));

        harness.assertOnBattlefield(player2, "Thassa, God of the Sea");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(originalLibrary.get(0), originalLibrary.get(1));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(originalLibrary.get(1), originalLibrary.get(0));
        harness.assertInGraveyard(player1, "Artisan's Sorrow");
    }

    @Test
    @DisplayName("Can destroy its controller's artifact and split the scry cards")
    void destroysOwnArtifactAndSplitsScry() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        prepare();
        Card first = new ArtisansSorrow();
        Card second = new ArtisansSorrow();
        Card third = new ArtisansSorrow();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Fountain of Youth"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Scry 2 looks at only one card when the library has one card")
    void scriesWithOneCardLibrary() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        prepare();
        Card onlyCard = new ArtisansSorrow();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Fountain of Youth"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Artisan's Sorrow");
    }

    @Test
    @DisplayName("An empty library does not prevent destruction or completion")
    void resolvesWithEmptyLibrary() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        prepare();
        harness.setLibrary(player1, List.of());

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Fountain of Youth"));

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Artisan's Sorrow");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void prepare() {
        harness.setHand(player1, List.of(new ArtisansSorrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
