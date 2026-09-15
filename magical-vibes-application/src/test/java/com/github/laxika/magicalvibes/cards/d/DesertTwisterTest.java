package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnergyFlux;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HornOfRamos;
import com.github.laxika.magicalvibes.cards.h.HornedTroll;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertTwister.class, EnergyFlux.class, Forest.class, HornOfRamos.class, HornedTroll.class})
class DesertTwisterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new HornedTroll());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Horned Troll");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Horned Troll");
        harness.assertInGraveyard(player2, "Horned Troll");
    }

    @Test
    @DisplayName("Can destroy a land — target is any permanent")
    void destroysLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Can destroy an artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new HornOfRamos());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Horn of Ramos");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Horn of Ramos");
    }

    @Test
    @DisplayName("Can destroy an enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new EnergyFlux());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Energy Flux");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Energy Flux");
    }

    @Test
    @DisplayName("Can destroy a permanent controlled by its caster")
    void destroysOwnPermanent() {
        harness.addToBattlefield(player1, new HornedTroll());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player1, "Horned Troll");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Horned Troll");
        harness.assertInGraveyard(player1, "Horned Troll");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTroll());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = target.getId();
        harness.castSorcery(player1, 0, targetId);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), target));
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
