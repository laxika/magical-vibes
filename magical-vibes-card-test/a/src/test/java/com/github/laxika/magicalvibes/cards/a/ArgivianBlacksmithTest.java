package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryTower;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgivianBlacksmith.class, Ornithopter.class, LightningBolt.class, GrizzlyBears.class, IvoryTower.class})
class ArgivianBlacksmithTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and shields an artifact creature from the next 2 damage")
    void shieldsArtifactCreature() {
        addReadyBlacksmith();
        Permanent target = addCreatureReady(player2, new Ornithopter());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents only the next 2 damage to an artifact creature")
    void preventsOnlyTheNextTwoDamage() {
        addReadyBlacksmith();
        Permanent target = addCreatureReady(player2, new Ornithopter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        addReadyBlacksmith();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyBlacksmith();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreatureArtifact() {
        addReadyBlacksmith();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IvoryTower());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent blacksmith = harness.addToBattlefieldAndReturn(player1, new ArgivianBlacksmith());
        Permanent target = addCreatureReady(player2, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blacksmith.isTapped()).isFalse();
    }

    @Test
    void cannotActivateTwiceWithoutUntapping() {
        addReadyBlacksmith();
        Permanent target = addCreatureReady(player1, new Ornithopter());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shieldsAddAndAreConsumedAcrossDamageEvents() {
        addReadyBlacksmith();
        addReadyBlacksmith();
        Permanent target = addCreatureReady(player1, new Ornithopter());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void unusedShieldExpiresAtEndOfTurn() {
        addReadyBlacksmith();
        Permanent target = addCreatureReady(player2, new Ornithopter());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void abilityResolvesAfterBlacksmithDies() {
        Permanent blacksmith = addCreatureReady(player1, new ArgivianBlacksmith());
        Permanent target = addCreatureReady(player2, new Ornithopter());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, blacksmith.getId());
        harness.assertInGraveyard(player1, "Argivian Blacksmith");
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    private void addReadyBlacksmith() {
        addCreatureReady(player1, new ArgivianBlacksmith());
    }
}
