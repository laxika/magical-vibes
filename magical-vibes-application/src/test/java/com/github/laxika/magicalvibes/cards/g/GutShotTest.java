package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GutShot.class, LlanowarElves.class, KarnLiberated.class})
class GutShotTest extends BaseCardTest {

    @Test
    @DisplayName("Gut Shot deals 1 damage to target player when paid with red mana")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Gut Shot deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can be cast by paying Phyrexian mana with 2 life instead of red mana")
    void canPayPhyrexianWithLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GutShot()));
        // No mana added — entire cost {R/P} paid with 2 life

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Player1 paid 2 life for Phyrexian mana
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 took 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Gut Shot goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gut Shot");
    }

    @Test
    @DisplayName("Gut Shot removes one loyalty counter from a targeted planeswalker")
    void damagesPlaneswalker() {
        var karn = harness.addToBattlefieldAndReturn(player2, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, karn.getId());

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Karn Liberated");
    }

    @Test
    @DisplayName("Gut Shot can target its caster and life payment is separate from damage")
    void canTargetCasterWhilePayingLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GutShot()));

        harness.castInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Gut Shot");
    }

    @Test
    @DisplayName("Cannot pay Gut Shot's Phyrexian cost with only one life and no red mana")
    void cannotPayLifeWhenInsufficient() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new GutShot()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Gut Shot");
        assertThat(gd.stack).isEmpty();
    }
}
