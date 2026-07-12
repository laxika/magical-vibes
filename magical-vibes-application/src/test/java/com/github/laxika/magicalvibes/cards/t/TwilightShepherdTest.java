package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwilightShepherdTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent shepherdOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Twilight Shepherd"))
                .findFirst().orElse(null);
    }

    // ===== ETB: return cards put into your graveyard from the battlefield this turn =====

    @Test
    @DisplayName("ETB returns a noncreature permanent that was put into your graveyard from the battlefield this turn")
    void etbReturnsNoncreatureCardFromBattlefieldThisTurn() {
        Card axe = new ObsidianBattleAxe();
        harness.addToBattlefield(player1, axe);

        // Destroy the artifact so it hits the graveyard from the battlefield this turn.
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Obsidian Battle-Axe"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(axe.getId()));

        // Cast Twilight Shepherd; its ETB should return the axe to hand.
        harness.setHand(player1, List.of(new TwilightShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(axe.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(axe.getId()));
    }

    @Test
    @DisplayName("ETB does not return cards that were not put into your graveyard from the battlefield this turn")
    void etbDoesNotReturnCardsNotFromBattlefield() {
        Card alreadyInGraveyard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));

        harness.setHand(player1, List.of(new TwilightShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(alreadyInGraveyard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(alreadyInGraveyard.getId()));
    }

    // ===== Persist =====

    @Test
    @DisplayName("Persist returns Twilight Shepherd with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new TwilightShepherd());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Twilight Shepherd"));
        resolveUntilInputOrEmpty();

        Permanent shepherd = shepherdOnBattlefield();
        assertThat(shepherd).isNotNull();
        assertThat(shepherd.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(shepherd.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Persist does not return Twilight Shepherd when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new TwilightShepherd());
        shepherd.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, shepherd.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Twilight Shepherd"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Twilight Shepherd"));
    }
}
