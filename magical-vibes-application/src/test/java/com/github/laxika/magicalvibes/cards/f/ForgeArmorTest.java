package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.c.CrystalShard;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgeArmor.class, CrystalShard.class, WeldingJar.class, AlphaMyr.class,
        GoblinStriker.class, AncientDen.class})
class ForgeArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters equal to the sacrificed artifact's mana value on target creature")
    void putsCountersEqualToSacrificedArtifactManaValue() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CrystalShard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.setHand(player1, List.of(new ForgeArmor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Crystal Shard");
    }

    @Test
    @DisplayName("A zero-mana-value artifact puts no counters on the target creature")
    void zeroManaValueArtifactPutsNoCounters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.setHand(player1, List.of(new ForgeArmor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Welding Jar");
    }

    @Test
    @DisplayName("Uses the selected artifact's mana value when multiple artifacts are available")
    void usesSelectedArtifactManaValueWhenMultipleArtifactsAreAvailable() {
        Permanent selectedArtifact = harness.addToBattlefieldAndReturn(player1, new CrystalShard());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.setHand(player1, List.of(new ForgeArmor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), selectedArtifact.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Crystal Shard");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(otherArtifact.getId()));
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact as the additional cost")
    void cannotSacrificeNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinStriker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        harness.setHand(player1, List.of(new ForgeArmor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientDen());

        harness.setHand(player1, List.of(new ForgeArmor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
