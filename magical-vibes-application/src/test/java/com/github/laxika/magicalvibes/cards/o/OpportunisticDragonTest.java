package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CaptainOfTheMists;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpportunisticDragon.class, CaptainOfTheMists.class, MindStone.class,
        Unsummon.class, GrizzlyBears.class})
class OpportunisticDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB steals an opposing Human and suppresses its abilities and combat")
    void stealsHumanAndSuppressesIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainOfTheMists());
        target.setSummoningSick(false);

        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.hasLostAllAbilities(gd, target)).isTrue();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("The stolen artifact and restrictions return when Opportunistic Dragon leaves")
    void effectsEndWhenDragonLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castDragon(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Opportunistic Dragon");
        bounceDragon(dragon);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.hasLostAllAbilities(gd, target)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    @Test
    @DisplayName("If Opportunistic Dragon leaves before its trigger resolves, nothing happens")
    void triggerDoesNothingIfDragonLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainOfTheMists());

        castDragon(target.getId());
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Opportunistic Dragon");
        bounceDragon(dragon);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.hasLostAllAbilities(gd, target)).isFalse();
        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.addToBattlefield(player2, new CaptainOfTheMists());
        harness.setHand(player1, List.of(new OpportunisticDragon()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDragon(UUID targetId) {
        harness.setHand(player1, List.of(new OpportunisticDragon()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void bounceDragon(Permanent dragon) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dragon.getId());
        harness.passBothPriorities();
    }
}
