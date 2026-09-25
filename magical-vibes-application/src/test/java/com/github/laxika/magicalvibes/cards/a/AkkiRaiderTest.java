package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiRaider.class, AkkiBlizzardHerder.class, Frostling.class, TendoIceBridge.class})
class AkkiRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when an opponent's land is sacrificed")
    void boostsWhenOpponentLandIsPutIntoGraveyard() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());

        Permanent frostling = findPermanent(player1, "Frostling");
        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities(); // Resolve Frostling's ability — Herder dies
        harness.passBothPriorities(); // Resolve Herder's trigger — Tendo Ice Bridge is sacrificed

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Akki Raider");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for the controller's own land too")
    void boostsWhenOwnLandIsPutIntoGraveyard() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());

        Permanent frostling = findPermanent(player1, "Frostling");
        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities(); // Resolve Frostling's ability — Herder dies
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when a non-land permanent dies")
    void doesNotTriggerOnNonLand() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player1, new Frostling());
        harness.addToBattlefield(player2, new Frostling());

        Permanent frostling = findPermanent(player1, "Frostling");
        Permanent target = findPermanent(player2, "Frostling");
        harness.activateAbility(player1, battlefieldIndex(frostling), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Frostling");
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+0 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());

        Permanent frostling = findPermanent(player1, "Frostling");
        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities(); // Resolve Frostling's ability — Herder dies
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets one boost for each land put into a graveyard")
    void boostsOncePerLandPutIntoGraveyard() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        harness.addToBattlefield(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player2, new TendoIceBridge());

        Permanent frostling = findPermanent(player1, "Frostling");
        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities(); // Resolve Frostling's ability — Herder dies
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(4);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
