package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GratuitousViolence;
import com.github.laxika.magicalvibes.cards.j.JohtullWurm;
import com.github.laxika.magicalvibes.cards.t.TimeBomb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarplusanYeti.class, BalduvianBears.class, KarplusanGiant.class, JohtullWurm.class,
        TimeBomb.class, GiantGrowth.class, GratuitousViolence.class})
class KarplusanYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Fight: 3/3 Yeti kills a 2/2 and survives with marked damage")
    void fightKillsSmallerCreature() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Bears takes 3 (lethal) and is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        // Yeti takes 2 and survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(yeti.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fight: both die when they deal mutual lethal damage")
    void fightMutualLethal() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent karplusanGiant = addCreatureReady(player2, new KarplusanGiant());

        harness.activateAbility(player1, 0, null, karplusanGiant.getId());
        harness.passBothPriorities();

        // Both 3/3s take 3 damage — both destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(karplusanGiant.getId()));
    }

    @Test
    @DisplayName("Source leaving before resolution still deals damage to the target")
    void sourceLeavingBeforeResolutionStillDamagesTarget() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent target = addCreatureReady(player2, new JohtullWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(yeti);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target and fight a creature it controls")
    void canFightOwnCreature() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(yeti.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target itself and deals twice its power when it fights itself")
    void canFightItself() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());

        harness.activateAbility(player1, 0, null, yeti.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(yeti.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Fight does nothing when the target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsFight() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent target = addCreatureReady(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(yeti.getId()));
        assertThat(yeti.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new KarplusanYeti());
        Permanent timeBomb = harness.addToBattlefieldAndReturn(player2, new TimeBomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, timeBomb.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new KarplusanYeti());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent other = addCreatureReady(player2, new BalduvianBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Source leaving before resolution still deals lethal damage to a small target")
    void sourceLeavingBeforeResolutionStillDamagesSmallTarget() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent target = addCreatureReady(player2, new KarplusanGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(yeti);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot activate the tap ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new KarplusanYeti());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Uses the source's power at resolution after Giant Growth")
    void usesSourcePowerAtResolution() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent wurm = addCreatureReady(player2, new JohtullWurm());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, wurm.getId());
        harness.castAndResolveInstant(player1, 0, yeti.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Karplusan Yeti");
        harness.assertInGraveyard(player2, "Johtull Wurm");
    }

    @Test
    @DisplayName("Uses the target's power at resolution after Giant Growth")
    void usesTargetPowerAtResolution() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Karplusan Yeti");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
        assertThat(yeti.getMarkedDamage()).isEqualTo(5);
        assertThat(bears.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Gratuitous Violence doubles only the damage dealt by its controller's Yeti")
    void damageMultiplierUsesEachDamageSourcesController() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addToBattlefield(player1, new GratuitousViolence());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.assertOnBattlefield(player1, "Karplusan Yeti");
        assertThat(yeti.getMarkedDamage()).isEqualTo(2);
    }
}
