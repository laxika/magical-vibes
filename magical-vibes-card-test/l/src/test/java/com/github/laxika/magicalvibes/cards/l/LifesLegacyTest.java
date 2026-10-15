package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifesLegacy.class, RuneclawBear.class, Ornithopter.class, TitanicGrowth.class, Negate.class})
class LifesLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the sacrificed creature's power")
    void drawsCardsEqualToSacrificedPower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        harness.assertInGraveyard(player1, "Life's Legacy");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's boosted power")
    void usesBoostedPower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Sacrificing a zero-power creature draws no cards")
    void zeroPowerDrawsNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Uses temporary power boosts from the battlefield after the creature is sacrificed")
    void usesTemporaryBoostedPower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth(), new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 6);
        harness.assertInGraveyard(player1, "Life's Legacy");
    }

    @Test
    @DisplayName("Sacrificing a negative-power creature draws no cards")
    void negativePowerDrawsNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        sacrifice.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Life's Legacy");
    }

    @Test
    @DisplayName("Countering the spell prevents the draw but does not undo the sacrifice")
    void counteredSpellKeepsSacrificePaid() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new LifesLegacy()));
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gd.stack.getFirst().getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Life's Legacy");
    }
}
