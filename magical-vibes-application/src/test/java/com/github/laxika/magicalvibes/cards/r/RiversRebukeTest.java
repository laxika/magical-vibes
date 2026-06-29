package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsTargetPlayerControlsToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiversRebukeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("River's Rebuke has correct effect configuration")
    void hasCorrectEffectConfiguration() {
        RiversRebuke card = new RiversRebuke();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnPermanentsTargetPlayerControlsToHandEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as SORCERY_SPELL targeting a player")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("River's Rebuke");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Returns all nonland permanents target player controls to their hand")
    void returnsAllNonlandPermanentsToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // All nonland permanents should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .noneMatch(p -> p.getCard().getName().equals("Angel's Feather"))
                .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));

        // All should be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Angel's Feather", "Glorious Anthem");
    }

    @Test
    @DisplayName("Does not return lands the target player controls")
    void doesNotReturnLands() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Land should still be on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));

        // Creature should be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not affect the other player's permanents")
    void doesNotAffectOtherPlayersNonlandPermanents() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1's permanents should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel's Feather"))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Player2's creature should be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Can target self to return own nonland permanents")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Land should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));

        // Nonland permanents should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Angel's Feather");
    }

    @Test
    @DisplayName("Works when target player has no nonland permanents")
    void worksWithNoNonlandPermanents() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Land should still be on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Works when target player has empty battlefield")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("River's Rebuke goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RiversRebuke()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("River's Rebuke"));
    }
}
