package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BorosMastiff;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LyevDecree.class, BorosMastiff.class, ZhurTaaDruid.class})
class LyevDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Detains both target creatures so neither can attack")
    void detainsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        cast(List.of(first.getId(), second.getId()));

        assertThatThrownBy(() -> declareAttack(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May detain a single creature (up to two)")
    void detainsSingleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        cast(List.of(bears.getId()));

        assertThatThrownBy(() -> declareAttack(List.of(bears)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detain wears off at the caster's next turn")
    void detainWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        cast(List.of(bears.getId()));

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(List.of(bears))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new BorosMastiff());
        harness.setHand(player1, List.of(new LyevDecree()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownBears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());

        cast(List.of());

        harness.assertInGraveyard(player1, "Lyev Decree");
        assertThatCode(() -> declareAttack(List.of(creature))).doesNotThrowAnyException();
    }

    @Test
    void detainedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        cast(List.of(blocker.getId()));
        Permanent attacker = addCreatureReady(player1, new BorosMastiff());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    void detainedCreatureCannotActivateManaAbility() {
        Permanent druid = addCreatureReady(player2, new ZhurTaaDruid());
        cast(List.of(druid.getId()));

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void detainDoesNotTapTheCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());

        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void detainPersistsThroughCleanupAndOpponentsTurnStart() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        cast(List.of(creature.getId()));

        gd.expireEndOfTurnFloatingEffects();
        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(List.of(creature)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void remainingTargetIsDetainedWhenOtherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        harness.setHand(player1, List.of(new LyevDecree()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(List.of(second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new BorosMastiff());
        harness.setHand(player1, List.of(new LyevDecree()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new LyevDecree()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void declareAttack(List<Permanent> creatures) {
        creatures.forEach(creature -> creature.setSummoningSick(false));
        List<Integer> indices = creatures.stream()
                .map(creature -> gd.playerBattlefields.get(player2.getId()).indexOf(creature))
                .toList();
        declareAttackers(player2, indices);
    }
}
