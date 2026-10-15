package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SoulsMajesty.class, CanyonMinotaur.class, Unsummon.class})
class SoulsMajestyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the target creature's power")
    void drawsEqualToPower() {
        harness.addToBattlefield(player1, new CanyonMinotaur());
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.setLibrary(player1, List.of(new CanyonMinotaur(), new CanyonMinotaur(), new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        UUID giantId = harness.getPermanentId(player1, "Canyon Minotaur");
        harness.castAndResolveSorcery(player1, 0, giantId);

        // Canyon Minotaur is a 3/3: cast one card, draw three.
        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new CanyonMinotaur());
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID enemyBearId = harness.getPermanentId(player2, "Canyon Minotaur");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enemyBearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Uses the creature's current power at resolution")
    void usesPowerAtResolution() {
        var creature = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.setLibrary(player1, List.of(new CanyonMinotaur(), new CanyonMinotaur(),
                new CanyonMinotaur(), new CanyonMinotaur(), new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, creature.getId());
        creature.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Soul's Majesty");
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -4})
    @DisplayName("Draws no cards for zero or negative power")
    void drawsNothingForNonpositivePower(int modifier) {
        var creature = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());
        creature.setPowerModifier(modifier);
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.setLibrary(player1, List.of(new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Soul's Majesty");
    }

    @Test
    @DisplayName("Draws nothing when the target leaves the battlefield in response")
    void drawsNothingWhenTargetLeavesBattlefield() {
        var creature = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());
        harness.setHand(player1, List.of(new SoulsMajesty()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new CanyonMinotaur(), new CanyonMinotaur(), new CanyonMinotaur()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Canyon Minotaur");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Soul's Majesty");
    }
}
