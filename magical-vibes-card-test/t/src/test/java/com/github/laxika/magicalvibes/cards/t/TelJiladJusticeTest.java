package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.g.GoblinCannon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladJustice.class, GoblinCannon.class, Tyrranax.class, DarksteelGargoyle.class})
class TelJiladJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and scries 2")
    void destroysArtifactAndScriesTwo() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoblinCannon()).getId();
        Card bottom = new TelJiladJustice();
        Card top = new Tyrranax();
        harness.setLibrary(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Goblin Cannon");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(bottom, top);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GoblinCannon());
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new Tyrranax()).getId();
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Fizzles without scrying if the target leaves before resolution")
    void fizzlesWithoutScryingIfTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoblinCannon()).getId();
        Card bottom = new TelJiladJustice();
        Card top = new Tyrranax();
        harness.setLibrary(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
    }

    @Test
    @DisplayName("Scries even when an indestructible artifact cannot be destroyed")
    void scriesWhenArtifactIsIndestructible() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle()).getId();
        Card first = new Tyrranax();
        Card second = new GoblinCannon();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
    }

    @Test
    @DisplayName("Can destroy its controller's artifact with an empty library")
    void destroysOwnArtifactWithEmptyLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GoblinCannon()).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Goblin Cannon");
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scries the only card when the library has fewer than two cards")
    void scriesSingleCard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoblinCannon()).getId();
        Card onlyCard = new Tyrranax();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Goblin Cannon");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Can reorder both scry cards on top or on bottom without moving the rest")
    void reordersBothScryCards(boolean putOnBottom) {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GoblinCannon()).getId();
        Card first = new Tyrranax();
        Card second = new GoblinCannon();
        Card remaining = new TelJiladJustice();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, putOnBottom
                ? new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0))
                : new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        if (putOnBottom) {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, second, first);
        } else {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, remaining);
        }
        harness.assertInGraveyard(player2, "Goblin Cannon");
        harness.assertInGraveyard(player1, "Tel-Jilad Justice");
    }
}
