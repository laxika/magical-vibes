package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ExposeToDaylight.class, AngelicChorus.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class ExposeToDaylightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact and scries 1")
    void destroysArtifactAndScriesOne() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        castExposeToDaylight(targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroys a target enchantment and scries 1")
    void destroysEnchantmentAndScriesOne() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setLibrary(player1, List.of(new Forest()));

        castExposeToDaylight(targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ExposeToDaylight()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy its controller's artifact and bottom the scried card")
    void destroysOwnArtifactAndBottomsScriedCard() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Card topCard = new Forest();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        castExposeToDaylight(harness.getPermanentId(player1, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        harness.assertInGraveyard(player1, "Expose to Daylight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not scry if the only target leaves before resolution")
    void doesNotScryWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ExposeToDaylight()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Expose to Daylight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroys its target even when its controller's library is empty")
    void destroysArtifactWithEmptyLibrary() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of());

        castExposeToDaylight(harness.getPermanentId(player2, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Expose to Daylight");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castExposeToDaylight(UUID targetId) {
        harness.setHand(player1, List.of(new ExposeToDaylight()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
