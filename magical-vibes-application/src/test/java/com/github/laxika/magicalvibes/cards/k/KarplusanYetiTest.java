package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.t.TimeBomb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarplusanYeti.class, BalduvianBears.class, KarplusanGiant.class, TimeBomb.class})
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
    @DisplayName("Source leaving before resolution prevents both creatures from fighting")
    void sourceLeavingBeforeResolutionPreventsFight() {
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent target = addCreatureReady(player2, new KarplusanGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(yeti);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.getMarkedDamage()).isZero();
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
        harness.addToBattlefield(player2, new TimeBomb());
        UUID timeBombId = harness.getPermanentId(player2, "Time Bomb");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, timeBombId))
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

}
