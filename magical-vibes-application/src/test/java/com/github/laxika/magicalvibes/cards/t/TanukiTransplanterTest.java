package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TanukiTransplanter.class, GrizzlyBears.class, HillGiant.class, BeastWithin.class, EnchantedEvening.class})
class TanukiTransplanterTest extends BaseCardTest {

    @Test
    void attackingUnconfiguredTransplanterAddsManaEqualToItsPower() {
        addCreatureReady(player1, new TanukiTransplanter());
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void attackingEquippedCreatureAddsManaEqualToItsPower() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent creature = addCreatureReady(player1, new HillGiant());
        transplanter.setAttachedTo(creature.getId());
        declareAttackers(List.of(1));
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void reconfigureAttachesAndUnattachesTheTransplanter() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(transplanter.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, transplanter)).isFalse();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(transplanter.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, transplanter)).isTrue();
    }

    @Test
    void equippedCreatureControlledByOpponentStillAddsManaToEquipmentController() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent creature = addCreatureReady(player2, new HillGiant());
        transplanter.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void unrelatedAttackerDoesNotAddMana() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        transplanter.setAttachedTo(host.getId());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void unconfiguredAttackerUsesPowerAtResolution() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        transplanter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void equippedAttackerUsesPowerAtResolution() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent host = addCreatureReady(player1, new HillGiant());
        transplanter.setAttachedTo(host.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        host.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void destroyedUnconfiguredAttackerUsesLastKnownPower() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        transplanter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.castInstant(player1, 0, transplanter.getId());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tanuki Transplanter");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    void destroyedEquippedAttackerUsesLastKnownPower() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent host = addCreatureReady(player1, new HillGiant());
        transplanter.setAttachedTo(host.getId());
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        host.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.castInstant(player1, 0, host.getId());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(transplanter.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    void generatedManaSurvivesPhasesButExpiresAtEndOfTurn() {
        addCreatureReady(player1, new TanukiTransplanter());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getPersistentMana(ManaColor.GREEN)).isZero();
    }

    @Test
    void reconfigureCannotTargetOpponentsCreatureOrItself() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent opponentCreature = addCreatureReady(player2, new TanukiTransplanter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, transplanter.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(transplanter.getAttachedTo()).isNull();
    }

    @Test
    void bothReconfigureModesRequireSorceryTiming() {
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent host = addCreatureReady(player1, new TanukiTransplanter());
        transplanter.setAttachedTo(host.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(transplanter.getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    @CardUsed({TanukiTransplanter.class, EnchantedEvening.class})
    void reconfigurePreservesOtherCardTypes() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent transplanter = addCreatureReady(player1, new TanukiTransplanter());
        Permanent host = addCreatureReady(player1, new TanukiTransplanter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(transplanter.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, transplanter)).isFalse();
        assertThat(gqs.isEnchantment(gd, transplanter)).isTrue();
    }
}
