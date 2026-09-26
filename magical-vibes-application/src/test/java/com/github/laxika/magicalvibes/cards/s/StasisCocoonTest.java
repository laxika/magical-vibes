package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnodetLurker;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.g.GemstoneArray;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StasisCocoon.class, AnodetLurker.class, GemstoneArray.class,
        DrossCrocodile.class})
class StasisCocoonTest extends BaseCardTest {

    @Test
    @DisplayName("Stasis Cocoon can enchant an artifact")
    void canEnchantArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GemstoneArray());
        harness.setHand(player1, List.of(new StasisCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof StasisCocoon
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Stasis Cocoon cannot enchant a nonartifact permanent")
    void cannotEnchantNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        harness.setHand(player1, List.of(new StasisCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Enchanted artifact cannot activate abilities")
    void enchantedArtifactCannotActivateAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        addAura(player2, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted artifact cannot activate mana abilities")
    void enchantedArtifactCannotActivateManaAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        artifact.setCounterCount(CounterType.CHARGE, 1);
        addAura(player2, artifact);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Artifact can activate abilities after Stasis Cocoon is removed")
    void artifactCanActivateAfterStasisCocoonRemoved() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GemstoneArray());
        Permanent aura = addAura(player2, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted artifact creature cannot attack")
    void enchantedArtifactCreatureCannotAttack() {
        Permanent artifact = addCreatureReady(player1, new AnodetLurker());
        addAura(player2, artifact);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted artifact creature cannot block")
    void enchantedArtifactCreatureCannotBlock() {
        Permanent artifact = addCreatureReady(player2, new AnodetLurker());
        addAura(player1, artifact);

        Permanent attacker = addCreatureReady(player1, new DrossCrocodile());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new StasisCocoon());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
