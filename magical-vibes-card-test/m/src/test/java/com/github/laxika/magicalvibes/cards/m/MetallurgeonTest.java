package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Metallurgeon.class, ObeliskOfBant.class, CylianElf.class, Naturalize.class})
class MetallurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to target artifact")
    void resolvingGrantsShield() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's artifact")
    void canTargetOpponentArtifact() {
        setupMetallurgeon();
        Permanent opponentArtifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Metallurgeon")
    void tapsOnActivation() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(findPermanent(player1, "Metallurgeon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifact() {
        setupMetallurgeon();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can regenerate itself as an artifact creature")
    void canRegenerateSelf() {
        Permanent metallurgeon = setupMetallurgeon();

        harness.activateAbility(player1, 0, null, metallurgeon.getId());
        harness.passBothPriorities();

        assertThat(metallurgeon.getRegenerationShield()).isEqualTo(1);
        assertThat(metallurgeon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Shield protects a noncreature artifact from destruction once and taps it only then")
    void protectsNoncreatureArtifactFromOneDestruction() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        assertThat(artifact.isTapped()).isFalse();

        harness.setHand(player1, java.util.List.of(new Naturalize(), new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ObeliskOfBant);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent metallurgeon = setupMetallurgeon();
        metallurgeon.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, metallurgeon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(metallurgeon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regenerating itself prevents destruction and clears marked damage")
    void regenerationClearsDamage() {
        Permanent metallurgeon = setupMetallurgeon();
        metallurgeon.setMarkedDamage(1);

        harness.activateAbility(player1, 0, null, metallurgeon.getId());
        harness.passBothPriorities();
        assertThat(metallurgeon.getMarkedDamage()).isEqualTo(1);

        harness.setHand(player1, java.util.List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, metallurgeon.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(metallurgeon);
        assertThat(metallurgeon.getMarkedDamage()).isZero();
        assertThat(metallurgeon.getRegenerationShield()).isZero();
        assertThat(metallurgeon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves even if Metallurgeon leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent metallurgeon = setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(metallurgeon);
        harness.passBothPriorities();

        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless mana cannot pay the white activation cost")
    void requiresWhiteMana() {
        Permanent metallurgeon = harness.addToBattlefieldAndReturn(player1, new Metallurgeon());
        metallurgeon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, metallurgeon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(metallurgeon.isTapped()).isFalse();
    }

    private Permanent setupMetallurgeon() {
        Permanent metallurgeon = harness.addToBattlefieldAndReturn(player1, new Metallurgeon());
        metallurgeon.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        return metallurgeon;
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ObeliskOfBant());
    }
}
