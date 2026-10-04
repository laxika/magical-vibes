package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GeyadroneDihada;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FodderTosser.class, OrnithopterOfParadise.class, GeyadroneDihada.class})
class FodderTosserTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and discarding deals 2 damage to a target player")
    void dealsDamageToTargetPlayer() {
        Permanent fodderTosser = harness.addToBattlefieldAndReturn(player1, new FodderTosser());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(fodderTosser.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefieldAndReturn(player1, new FodderTosser());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefieldAndReturn(player1, new FodderTosser());
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        UUID creatureId = harness.getPermanentId(player2, "Ornithopter of Paradise");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new FodderTosser());
        Permanent dihada = harness.addToBattlefieldAndReturn(player2, new GeyadroneDihada());
        dihada.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));

        harness.activateAbility(player1, 0, null, dihada.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetController() {
        harness.addToBattlefield(player1, new FodderTosser());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void paysCostsBeforeDamageResolves() {
        Permanent tosser = harness.addToBattlefieldAndReturn(player1, new FodderTosser());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FodderTosser(), new OrnithopterOfParadise()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(tosser.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Fodder Tosser");
        harness.assertNotInHand(player1, "Fodder Tosser");
        harness.assertInHand(player1, "Ornithopter of Paradise");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Ornithopter of Paradise");
    }

    @Test
    void cannotActivateTappedArtifact() {
        Permanent tosser = harness.addToBattlefieldAndReturn(player1, new FodderTosser());
        tosser.tap();
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ornithopter of Paradise");
        harness.assertNotInGraveyard(player1, "Ornithopter of Paradise");
    }
}
