package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Teleportal.class, DrudgeBeetle.class})
class TeleportalTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gets +1/+0 and can't be blocked")
    void targetsOwnCreature() {
        Permanent target = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent target = addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload boosts and makes every creature you control unblockable")
    void overloadAffectsAllOwnCreatures() {
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);

        first.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by no creatures");
    }

    @Test
    @DisplayName("The temporary effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Overload requires its alternate cost")
    void overloadRequiresFullCost() {
        addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal casting affects only the targeted creature")
    void normalCastLeavesOtherCreaturesUnaffected() {
        Permanent target = addCreatureReady(player1);
        Permanent other = addCreatureReady(player1);
        Permanent opponent = addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(other.isCantBeBlocked()).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(opponent.isCantBeBlocked()).isFalse();
        other.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Overload prevents blocking later creatures without boosting them")
    void overloadAffectsLaterCreaturesOnlyForBlocking() {
        Permanent original = addCreatureReady(player1);
        addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addOverloadMana();
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        Permanent later = addCreatureReady(player1);

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        later.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by no creatures");
    }

    @Test
    @DisplayName("Overload can resolve with no creatures or targets")
    void overloadCanResolveOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new Teleportal()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Teleportal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Overload's boost and blocking restriction expire at end of turn")
    void overloadEffectsWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1);
        addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addOverloadMana();
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A removed target makes the normal spell resolve without affecting other creatures")
    void removedTargetLeavesOtherCreaturesUnaffected() {
        Permanent target = addCreatureReady(player1);
        Permanent other = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(other.isCantBeBlocked()).isFalse();
        harness.assertInGraveyard(player1, "Teleportal");
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new DrudgeBeetle());
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addOverloadMana() {
        addNormalMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
