package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({RelentlessSkaabs.class, DoomBlade.class, GrizzlyBears.class, Shock.class})
class RelentlessSkaabsTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast by exiling a creature card from graveyard")
    void castExilesCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Relentless Skaabs");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast without a creature card in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card to pay the additional cost")
    void cannotExileNonCreatureCard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Undying returns Relentless Skaabs with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent skaabs = harness.addToBattlefieldAndReturn(player1, new RelentlessSkaabs());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, skaabs.getId());
        resolveAllTriggers();

        Permanent returnedSkaabs = findPermanent(player1, "Relentless Skaabs");
        assertThat(returnedSkaabs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedSkaabs.getEffectivePower()).isEqualTo(5);
        assertThat(returnedSkaabs.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Relentless Skaabs");
    }

    @Test
    @DisplayName("Undying does not return Relentless Skaabs when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent skaabs = harness.addToBattlefieldAndReturn(player1, new RelentlessSkaabs());
        skaabs.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, skaabs.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Relentless Skaabs");
        harness.assertInGraveyard(player1, "Relentless Skaabs");
    }

    @Test
    @DisplayName("An opponent's graveyard cannot pay the additional cost")
    void cannotUseOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new RelentlessSkaabs()));
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Relentless Skaabs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting exiles only the selected creature and resolves normally")
    void castExilesOnlySelectedCreature() {
        RelentlessSkaabs first = new RelentlessSkaabs();
        RelentlessSkaabs selected = new RelentlessSkaabs();
        harness.setGraveyard(player1, List.of(first, selected));
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGraveyardExile(player1, 0, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(selected);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Relentless Skaabs");
        assertThat(findPermanent(player1, "Relentless Skaabs")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Undying still triggers when the creature has a -1/-1 counter")
    void undyingReturnsWithOtherCounters() {
        Permanent skaabs = harness.addToBattlefieldAndReturn(player1, new RelentlessSkaabs());
        skaabs.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, skaabs.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Relentless Skaabs");
        assertThat(returned.getId()).isNotEqualTo(skaabs.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Relentless Skaabs");
    }

    @Test
    @DisplayName("The counter from undying prevents a second return")
    void undyingDoesNotReturnTwice() {
        Permanent skaabs = harness.addToBattlefieldAndReturn(player1, new RelentlessSkaabs());
        harness.setHand(player2, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player2, 0, skaabs.getId());
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Relentless Skaabs");
        harness.castInstant(player2, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Relentless Skaabs");
        harness.assertInGraveyard(player1, "Relentless Skaabs");
    }
}
