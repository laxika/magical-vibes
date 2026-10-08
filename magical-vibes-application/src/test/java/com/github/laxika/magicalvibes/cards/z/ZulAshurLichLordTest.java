package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FanaticalFirebrand;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZulAshurLichLord.class, GiantGrowth.class, GrizzlyBears.class, Shock.class,
        WalkingCorpse.class, FanaticalFirebrand.class})
class ZulAshurLichLordTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay 2 life")
    void wardCountersUnpaidSpell() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, zul.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying 2 life lets an opponent's spell targeting Zul Ashur resolve")
    void payingWardLifeLetsSpellResolve() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, zul.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gqs.getEffectivePower(gd, zul)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zul)).isEqualTo(5);
    }

    @Test
    @DisplayName("The tap ability grants a later cast of a targeted Zombie creature card")
    void grantsTargetedZombieCreatureGraveyardCast() {
        addCreatureReady(player1, new ZulAshurLichLord());
        WalkingCorpse zombie = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("The tap ability cannot target a non-Zombie creature card")
    void cannotTargetNonZombieCreatureCard() {
        addCreatureReady(player1, new ZulAshurLichLord());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ward does not charge life for your own spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, zul.getId());

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, zul)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability when life is not paid")
    void wardCountersUnpaidActivatedAbility() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        addCreatureReady(player2, new FanaticalFirebrand());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, zul.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(zul.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Fanatical Firebrand");
    }

    @Test
    @DisplayName("Paying ward allows an opponent's activated ability to resolve")
    void payingWardAllowsActivatedAbility() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        addCreatureReady(player2, new FanaticalFirebrand());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, zul.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(zul.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Zul Ashur, Lich Lord");
    }

    @Test
    @DisplayName("An opponent with only 1 life cannot pay ward")
    void insufficientLifeCountersSpell() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        harness.setLife(player2, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, zul.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 1);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Zul Ashur, Lich Lord");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Zul Ashur cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new ZulAshurLichLord());
        ZulAshurLichLord zombie = new ZulAshurLichLord();
        harness.setGraveyard(player1, List.of(zombie));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target a Zombie in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addCreatureReady(player1, new ZulAshurLichLord());
        ZulAshurLichLord zombie = new ZulAshurLichLord();
        harness.setGraveyard(player2, List.of(zombie));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The permission does not waive the Zombie's mana cost")
    void graveyardCastRequiresMana() {
        grantWalkingCorpseCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("The permission does not allow a creature cast outside a main phase")
    void graveyardCastRespectsCreatureTiming() {
        grantWalkingCorpseCast();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGIN_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Only the targeted Zombie gains permission to be cast")
    void permissionDoesNotExtendToOtherZombies() {
        addCreatureReady(player1, new ZulAshurLichLord());
        WalkingCorpse target = new WalkingCorpse();
        WalkingCorpse other = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("The graveyard casting permission expires at the end of the turn")
    void graveyardPermissionExpires() {
        grantWalkingCorpseCast();
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Activating taps Zul Ashur and prevents a second activation")
    void tapCostPreventsSecondActivation() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        ZulAshurLichLord zombie = new ZulAshurLichLord();
        harness.setGraveyard(player1, List.of(zombie));

        harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(zul.isTapped()).isTrue();
        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still grants permission if Zul Ashur dies before resolution")
    void sourceLeavingDoesNotPreventPermission() {
        Permanent zul = addCreatureReady(player1, new ZulAshurLichLord());
        WalkingCorpse zombie = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD);
        harness.castAndResolveInstant(player1, 0, zul.getId());
        harness.assertInGraveyard(player1, "Zul Ashur, Lich Lord");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, zombie.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
    }
    private void grantWalkingCorpseCast() {
        addCreatureReady(player1, new ZulAshurLichLord());
        WalkingCorpse zombie = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(zombie));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, zombie.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }
}
