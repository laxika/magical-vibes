package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BarbedBloodletter.class, GrizzlyBears.class, GiantSpider.class})
class BarbedBloodletterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it wither")
    void entersAttachedAndGrantsWither() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodletter(creature);

        Permanent bloodletter = findPermanent(player1, "Barbed Bloodletter");
        assertThat(bloodletter.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Granted wither makes combat damage apply -1/-1 counters")
    void grantedWitherDealsMinusCounters() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castBloodletter(attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Granted wither wears off at end of turn")
    void grantedWitherWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodletter(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarbedBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Re-equipping moves the bonus but leaves wither on the original creature")
    void reequippingDoesNotMoveWither() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent next = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodletter(original);
        Permanent equipment = findPermanent(player1, "Barbed Bloodletter");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(equipment),
                null, next.getId());
        resolveAllTriggers();

        assertThat(equipment.getAttachedTo()).isEqualTo(next.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, next)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, next)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, next, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("The target gains wither even if the Equipment leaves before the trigger resolves")
    void grantsWitherWithoutEquipmentOnBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarbedBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Barbed Bloodletter");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, equipment));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows casting on an opponent's turn without a creature to attach to")
    void flashWithoutCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new BarbedBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Barbed Bloodletter").getAttachedTo()).isNull();
    }

    private void castBloodletter(Permanent target) {
        harness.setHand(player1, List.of(new BarbedBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
