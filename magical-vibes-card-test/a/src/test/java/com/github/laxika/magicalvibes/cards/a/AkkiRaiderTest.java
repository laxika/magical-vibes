package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiRaider.class, AkkiBlizzardHerder.class, Frostling.class, TendoIceBridge.class, SongOfTheDryads.class})
class AkkiRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when an opponent's land is sacrificed")
    void boostsWhenOpponentLandIsPutIntoGraveyard() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());

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
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());

        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities(); // Resolve Frostling's ability — Herder dies
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when a non-land permanent dies")
    void doesNotTriggerOnNonLand() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        harness.addToBattlefield(player2, new Frostling());

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
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());

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
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player2, new TendoIceBridge());

        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities(); // Resolve Frostling's ability — Herder dies
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Raider gets its own boost when a land dies")
    void boostsEachRaiderIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());

        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("A pending boost does not move to another Raider when its source dies")
    void pendingBoostDoesNotAffectAnotherRaider() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        Permanent firstFrostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent secondFrostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new TendoIceBridge());

        harness.activateAbility(player1, battlefieldIndex(firstFrostling), null, herder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.activateAbility(player1, battlefieldIndex(secondFrostling), null, source.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Akki Raider");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when a printed nonland that became a land is sacrificed")
    void triggersForPermanentThatBecameLand() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new AkkiRaider());
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        Permanent herder = harness.addToBattlefieldAndReturn(player2, new AkkiBlizzardHerder());
        Permanent transformed = harness.addToBattlefieldAndReturn(player2, new Frostling());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, transformed.getId());
        harness.passBothPriorities();
        assertThat(gqs.isLand(gd, transformed)).isTrue();

        harness.activateAbility(player1, battlefieldIndex(frostling), null, herder.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Frostling");
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
