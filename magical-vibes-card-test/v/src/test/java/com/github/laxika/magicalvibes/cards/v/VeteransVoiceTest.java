package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EnslavedScout;
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

@CardUsed({VeteransVoice.class, EnslavedScout.class})
class VeteransVoiceTest extends BaseCardTest {

    private Permanent host;
    private Permanent aura;
    private Permanent other;

    private void setupAura() {
        host = addCreatureReady(player1, new EnslavedScout());
        aura = harness.addToBattlefieldAndReturn(player1, new VeteransVoice());
        aura.setAttachedTo(host.getId());

        other = addCreatureReady(player1, new EnslavedScout());
    }

    @Test
    @DisplayName("Can enchant a creature you control")
    void enchantsCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        harness.setHand(player1, List.of(new VeteransVoice()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a creature an opponent controls")
    void cannotEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EnslavedScout());
        harness.setHand(player1, List.of(new VeteransVoice()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping the enchanted creature gives another target creature +2/+1")
    void boostsOtherCreature() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(aura.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void boostsOpponentsCreature() {
        setupAura();
        Permanent opponentCreature = addCreatureReady(player2, new EnslavedScout());

        harness.activateAbility(player1, 1, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability resolves if the Aura leaves before resolution")
    void resolvesAfterAuraLeavesBattlefield() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, aura));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the enchanted creature is tapped")
    void cannotActivateWhileHostTapped() {
        setupAura();
        host.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enchanted creature itself is an illegal target")
    void rejectsEnchantedCreatureAsTarget() {
        setupAura();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(host.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A noncreature permanent is an illegal target")
    void rejectsNoncreatureAsTarget() {
        setupAura();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(host.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick enchanted creature can pay the tapping cost")
    void canTapSummoningSickHost() {
        setupAura();
        host.setSummoningSick(true);

        harness.activateAbility(player1, 1, null, other.getId());
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Aura being tapped does not prevent activation")
    void canActivateTappedAura() {
        setupAura();
        aura.tap();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Untapping the host permits another activation and the boosts stack")
    void canActivateAgainAfterHostUntaps() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        host.untap();
        harness.activateAbility(player1, 1, null, other.getId());
        resolveAllTriggers();

        assertThat(host.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Moving the Aura onto the target does not make that target the creature tapped for the cost")
    void targetRemainsLegalAfterAuraMovesOntoIt() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        aura.setAttachedTo(other.getId());
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability still resolves after the creature tapped for its cost dies")
    void resolvesAfterHostDies() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host, aura);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Aura goes to the graveyard when its controller loses control of the enchanted creature")
    void auraDiesWhenHostChangesController() {
        setupAura();

        gd.playerBattlefields.get(player1.getId()).remove(host);
        gd.playerBattlefields.get(player2.getId()).add(host);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        harness.assertInGraveyard(player1, "Veteran's Voice");
    }

    @Test
    @DisplayName("Losing the target does not refund the creature tapped to pay the cost")
    void targetDiesBeforeResolution() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, other));
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }
}
