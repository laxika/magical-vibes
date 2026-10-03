package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CetaSanctuary.class, Cromat.class, Forest.class, GrizzlyBears.class, GoblinRaider.class, Mountain.class})
class CetaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("A single red and green permanent enables two draws")
    void singleMulticoloredPermanentEnablesTwoDraws() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new Cromat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Basic lands and opponents' colored permanents do not enable the trigger")
    void onlyControlledPermanentColorsEnableTrigger() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Cromat());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does nothing if the last qualifying permanent leaves before resolution")
    void conditionIsRecheckedAtResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new GoblinRaider());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Goblin Raider"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draw count increases if the second color appears before resolution")
    void secondColorAppearingBeforeResolutionEnablesTwoDraws() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new GoblinRaider());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("A qualifying permanent arriving after upkeep begins cannot create a trigger")
    void conditionMustBeMetWhenUpkeepBegins() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new GoblinRaider());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void onlyTriggersDuringControllersUpkeep() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new Cromat());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws one card and discards one when you control a red permanent only")
    void drawsOneAndDiscardsOneWithRedPermanentOnly() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new GoblinRaider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Draws one card and discards one when you control a green permanent only")
    void drawsOneAndDiscardsOneWithGreenPermanentOnly() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Draws two cards and discards one when you control both a red and a green permanent")
    void drawsTwoAndDiscardsOneWithRedAndGreenPermanents() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new GoblinRaider());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Falls back to one draw when the green permanent leaves before resolution")
    void fallsBackToOneDrawWhenGreenPermanentLeavesBeforeResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());
        harness.addToBattlefield(player1, new GoblinRaider());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw or discard without a red or green permanent")
    void doesNotDrawOrDiscardWithoutRedOrGreenPermanent() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new CetaSanctuary());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
