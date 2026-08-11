package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SatyrPiperTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability makes target creature must be blocked")
    void activatedAbilityMakesTargetMustBeBlocked() {
        addReadyPiper(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Must-be-blocked requirement wears off at end of turn")
    void mustBeBlockedRequirementWearsOff() {
        addReadyPiper(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyPiper(player1);
        Permanent target = addPermanent(player2, new DarksteelRelic());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPiper(Player player) {
        return addReadyPermanent(player, new SatyrPiper());
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addPermanent(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
