package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({Alarum.class, BayFalcon.class, Plains.class})
class AlarumTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts the target nonattacking creature")
    void untapsAndBoostsTarget() {
        Permanent target = addTappedCreature(player2);

        castAlarum(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target an already untapped nonattacking creature")
    void canTargetUntappedCreature() {
        Permanent target = addCreatureReady(player2, new BayFalcon());

        castAlarum(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        castAlarum(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target an attacking creature")
    void cannotTargetAttacker() {
        addTappedCreature(player1); // a legal target must exist for the spell to be castable
        Permanent attacker = addCreatureReady(player1, new BayFalcon());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        prepareAlarum();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonattacking");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        prepareAlarum();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can boost a blocking creature without removing it from combat")
    void canTargetBlockingCreature() {
        Permanent attacker = addCreatureReady(player1, new BayFalcon());
        Permanent blocker = addCreatureReady(player2, new BayFalcon());
        declareAttackersAndPrepareBlockers(List.of(0));
        prepareAlarum();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(blocker.getPowerModifier()).isEqualTo(1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the chosen creature is untapped and boosted")
    void affectsOnlyChosenCreature() {
        Permanent target = addTappedCreature(player1);
        Permanent other = addTappedCreature(player1);

        castAlarum(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not affect a creature that leaves and returns before resolution")
    void doesNotAffectReturnedCreature() {
        Permanent target = addTappedCreature(player2);
        prepareAlarum();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        returned.tap();

        harness.passBothPriorities();

        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Alarum");
        assertThat(gd.stack).isEmpty();
    }

    private void castAlarum(Permanent target) {
        prepareAlarum();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareAlarum() {
        harness.setHand(player1, List.of(new Alarum()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = addCreatureReady(player, new BayFalcon());
        perm.tap();
        return perm;
    }
}
