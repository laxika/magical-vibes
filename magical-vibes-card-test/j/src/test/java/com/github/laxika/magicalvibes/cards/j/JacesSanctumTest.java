package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArtificersEpiphany;
import com.github.laxika.magicalvibes.cards.h.HealingHands;
import com.github.laxika.magicalvibes.cards.m.MaritimeGuard;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JacesSanctum.class, ArtificersEpiphany.class, HealingHands.class, MaritimeGuard.class})
class JacesSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Sorcery spells you cast cost {1} less")
    void sorceryCostsOneLess() {
        harness.addToBattlefield(player1, new JacesSanctum());
        // Healing Hands {2}{W} reduced to {1}{W}
        harness.setHand(player1, List.of(new HealingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Healing Hands"));
    }

    @Test
    @DisplayName("Instant spells you cast cost {1} less")
    void instantCostsOneLess() {
        harness.addToBattlefield(player1, new JacesSanctum());
        // Artificer's Epiphany {2}{U} reduced to {1}{U}
        harness.setHand(player1, List.of(new ArtificersEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Artificer's Epiphany"));
    }

    @Test
    @DisplayName("Reduction does not apply to opponents' instants")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player2, List.of(new ArtificersEpiphany()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new MaritimeGuard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting an instant triggers scry 1")
    void instantTriggersScry() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new ArtificersEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Jace's Sanctum"));
    }

    @Test
    @DisplayName("Resolving the scry trigger enters the scry interaction")
    void scryTriggerResolvesIntoScryState() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new ArtificersEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger scry")
    void creatureSpellDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new MaritimeGuard()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger scry")
    void opponentInstantDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player2, List.of(new ArtificersEpiphany()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Scry resolves before the sorcery and can keep or bottom its controller's top card")
    void sorceryScryDeterminesCardDrawn(boolean keepOnTop) {
        harness.addToBattlefield(player1, new JacesSanctum());
        MaritimeGuard top = new MaritimeGuard();
        MaritimeGuard second = new MaritimeGuard();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new HealingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player1.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                keepOnTop ? List.of(0) : List.of(), keepOnTop ? List.of() : List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keepOnTop ? top : second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keepOnTop ? second : top);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Multiple Sanctums stack their reductions and each triggers scry separately")
    void multipleSanctumsReduceCostAndTriggerSeparately() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.addToBattlefield(player1, new JacesSanctum());
        MaritimeGuard top = new MaritimeGuard();
        MaritimeGuard second = new MaritimeGuard();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new HealingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Excess generic reduction does not pay colored mana")
    void excessReductionCannotPayColoredMana() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new ArtificersEpiphany()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry with an empty library finishes without prompting or resolving the spell")
    void emptyLibraryScryFinishes() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ArtificersEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Enchantment spells are neither reduced nor trigger scry")
    void enchantmentDoesNotBenefit() {
        harness.addToBattlefield(player1, new JacesSanctum());
        harness.setHand(player1, List.of(new JacesSanctum()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
