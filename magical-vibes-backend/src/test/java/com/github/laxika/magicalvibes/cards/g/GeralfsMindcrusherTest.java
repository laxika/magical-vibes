package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GeralfsMindcrusherTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gd = harness.getGameData();
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent mindcrusherOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Geralf's Mindcrusher"))
                .findFirst().orElse(null);
    }

    private void castMindcrusher(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GeralfsMindcrusher()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Geralf's Mindcrusher has an ETB mill-five effect that needs a target")
    void hasEtbMillEffect() {
        GeralfsMindcrusher card = new GeralfsMindcrusher();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        MillTargetPlayerEffect effect =
                (MillTargetPlayerEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(5);
    }

    // ===== Resolving creature spell =====

    @Test
    @DisplayName("Resolving puts Geralf's Mindcrusher on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castMindcrusher(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Geralf's Mindcrusher"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    // ===== ETB mill =====

    @Test
    @DisplayName("ETB trigger mills five cards from target player's library")
    void etbMillsFiveCards() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castMindcrusher(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castMindcrusher(player1.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than five")
    void millsOnlyRemainingWhenLibrarySmall() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 3) {
            deck.removeFirst();
        }

        castMindcrusher(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    // ===== Undying =====

    @Test
    @DisplayName("Undying returns Geralf's Mindcrusher with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new GeralfsMindcrusher());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Two bolts (3 + 3 = 6) to kill the 5/5.
        UUID mindcrusherId = harness.getPermanentId(player1, "Geralf's Mindcrusher");
        harness.castInstant(player1, 0, mindcrusherId);
        resolveUntilInputOrEmpty();
        harness.castInstant(player1, 0, mindcrusherId);
        resolveUntilInputOrEmpty();

        // Undying returned it with a +1/+1 counter and its ETB ability now asks for a target.
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        Permanent mindcrusher = mindcrusherOnBattlefield();
        assertThat(mindcrusher).isNotNull();
        assertThat(mindcrusher.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(mindcrusher.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("Undying return re-triggers the ETB, milling the chosen player five cards")
    void undyingReturnRetriggersEtb() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.addToBattlefield(player1, new GeralfsMindcrusher());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID mindcrusherId = harness.getPermanentId(player1, "Geralf's Mindcrusher");
        harness.castInstant(player1, 0, mindcrusherId);
        resolveUntilInputOrEmpty();
        harness.castInstant(player1, 0, mindcrusherId);
        resolveUntilInputOrEmpty();

        // Choose the opponent as the target of the returned Mindcrusher's ETB mill.
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Undying does not return Geralf's Mindcrusher when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent mindcrusher = harness.addToBattlefieldAndReturn(player1, new GeralfsMindcrusher());
        mindcrusher.setPlusOnePlusOneCounters(1); // now 6/6
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Two bolts (3 + 3 = 6 damage) to kill the 6/6.
        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveUntilInputOrEmpty();
        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Geralf's Mindcrusher"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Geralf's Mindcrusher"));
    }
}
