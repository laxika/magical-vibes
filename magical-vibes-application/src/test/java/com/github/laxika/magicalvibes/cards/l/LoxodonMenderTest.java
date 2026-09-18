package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoxodonMender.class, Ornithopter.class, LeoninScimitar.class, LeoninSkyhunter.class})
class LoxodonMenderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to target artifact")
    void resolvingGrantsShield() {
        setupLoxodonMender();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's artifact")
    void canTargetOpponentArtifact() {
        setupLoxodonMender();
        Permanent opponentArtifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate a noncreature artifact")
    void canTargetNonCreatureArtifact() {
        setupLoxodonMender();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Loxodon Mender")
    void tapsOnActivation() {
        Permanent mender = setupLoxodonMender();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(mender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        setupLoxodonMender();
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation requires one white mana")
    void requiresWhiteMana() {
        Permanent mender = setupLoxodonMender(false);
        Permanent artifact = addArtifact(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(mender.isTapped()).isFalse();
        assertThat(artifact.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability fizzles if the target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        setupLoxodonMender();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(artifact.getRegenerationShield()).isZero();
    }

    private Permanent setupLoxodonMender() {
        return setupLoxodonMender(true);
    }

    private Permanent setupLoxodonMender(boolean addMana) {
        Permanent mender = harness.addToBattlefieldAndReturn(player1, new LoxodonMender());
        mender.setSummoningSick(false);
        if (addMana) {
            harness.addMana(player1, ManaColor.WHITE, 1);
        }
        harness.forceActivePlayer(player1);
        return mender;
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Ornithopter());
    }
}
