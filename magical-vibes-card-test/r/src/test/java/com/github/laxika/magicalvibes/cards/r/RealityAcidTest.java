package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FuryCharm;
import com.github.laxika.magicalvibes.cards.s.Saltblast;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealityAcid.class, FuryCharm.class, Saltblast.class, UrborgTombOfYawgmoth.class})
class RealityAcidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters")
    void entersWithTimeCounters() {
        Permanent urborg = addTargetPermanent(player2);

        harness.setHand(player1, List.of(new RealityAcid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0, urborg.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Reality Acid");
        assertThat(aura.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(aura.getAttachedTo()).isEqualTo(urborg.getId());
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent urborg = addTargetPermanent(player2);
        Permanent aura = addAuraAttachedTo(player1, urborg);
        aura.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Does not remove a time counter during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        Permanent urborg = addTargetPermanent(player2);
        Permanent aura = addAuraAttachedTo(player1, urborg);
        aura.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent urborg = addTargetPermanent(player2);
        Permanent aura = addAuraAttachedTo(player1, urborg);
        aura.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Reality Acid");
        harness.assertInGraveyard(player1, "Reality Acid");
    }

    @Test
    @DisplayName("Does not sacrifice itself when it has no time counters")
    void noTimeCountersDoesNotTriggerSacrifice() {
        Permanent urborg = addTargetPermanent(player2);
        Permanent aura = addAuraAttachedTo(player1, urborg);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.TIME)).isZero();
        harness.assertOnBattlefield(player1, "Reality Acid");
    }

    @Test
    @DisplayName("When it leaves the battlefield, the enchanted permanent is sacrificed")
    void sacrificesEnchantedPermanentWhenAuraLeaves() {
        Permanent urborg = addTargetPermanent(player2);
        Permanent aura = addAuraAttachedTo(player1, urborg);

        harness.setHand(player1, List.of(new Saltblast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Removing the last time counter with another spell triggers vanishing")
    void externalCounterRemovalCausesSacrifice() {
        Permanent urborg = addTargetPermanent(player2);
        Permanent aura = addAuraAttachedTo(player1, urborg);
        aura.setCounterCount(CounterType.TIME, 2);

        harness.setHand(player1, List.of(new FuryCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 2, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Reality Acid");
        harness.assertInGraveyard(player1, "Reality Acid");
        harness.assertNotOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Vanishing does not put an upkeep ability on the stack without time counters")
    void noTimeCountersDoesNotCreateUpkeepTrigger() {
        Permanent urborg = addTargetPermanent(player2);
        addAuraAttachedTo(player1, urborg);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Reality Acid");
        harness.assertOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Vanishing sacrifices the enchanted permanent even when the Aura controller owns it")
    void vanishingSacrificesOwnEnchantedPermanent() {
        Permanent urborg = addTargetPermanent(player1);
        Permanent aura = addAuraAttachedTo(player1, urborg);
        aura.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Reality Acid");
        harness.assertInGraveyard(player1, "Reality Acid");
        harness.assertNotOnBattlefield(player1, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player1, "Urborg, Tomb of Yawgmoth");
    }

    private Permanent addTargetPermanent(com.github.laxika.magicalvibes.model.Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new UrborgTombOfYawgmoth());
    }

    private Permanent addAuraAttachedTo(com.github.laxika.magicalvibes.model.Player controller,
            Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new RealityAcid());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }
}
