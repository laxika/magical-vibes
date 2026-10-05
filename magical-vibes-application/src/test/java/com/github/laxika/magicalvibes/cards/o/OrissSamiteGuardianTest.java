package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.t.TakePossession;
import com.github.laxika.magicalvibes.cards.t.TaroxBladewing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrissSamiteGuardian.class, NessianCourser.class, Ghostfire.class, DakmorSalvage.class,
        TaroxBladewing.class, TakePossession.class})
class OrissSamiteGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability prevents all damage to target creature this turn")
    void preventsAllDamageToTargetCreature() {
        Permanent oriss = addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent creature = addCreatureReady(player1, new NessianCourser());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(oriss.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability prevents damage only to the chosen creature")
    void preventsDamageOnlyToChosenCreature() {
        addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent protectedCreature = addCreatureReady(player1, new NessianCourser());
        Permanent unprotectedCreature = addCreatureReady(player1, new NessianCourser());

        harness.activateAbility(player1, 0, null, protectedCreature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, unprotectedCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature)
                .doesNotContain(unprotectedCreature);
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void tapAbilityRequiresCreatureTarget() {
        Permanent oriss = addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DakmorSalvage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(oriss.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur discards another Oriss and locks the target player")
    void grandeurLocksTargetPlayer() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent source = addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent creature = addCreatureReady(player2, new NessianCourser());
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new NessianCourser()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .doesNotContain(indexOf(player2, creature));
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, source));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Oriss, Samite Guardian"));
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Grandeur cannot be paid with a different card")
    void grandeurRequiresAnotherOriss() {
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        harness.setHand(player1, List.of(new NessianCourser()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Prevention protects an opponent's creature from multiple damage events")
    void protectsOpponentsCreatureFromMultipleDamageEvents() {
        addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent creature = addCreatureReady(player2, new NessianCourser());
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Ghostfire(), new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevention applies to combat damage as well as spell damage")
    void preventsCombatDamage() {
        addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent blocker = addCreatureReady(player1, new NessianCourser());
        addCreatureReady(player2, new NessianCourser());
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, blocker), 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new OrissSamiteGuardian());
        Permanent creature = addCreatureReady(player1, new NessianCourser());
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Grandeur works while Oriss is tapped and summoning sick and can target its controller")
    void grandeurCanTargetControllerWithoutTapCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OrissSamiteGuardian());
        source.setTapped(true);
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Grandeur permits land plays and activated abilities")
    void grandeurDoesNotPreventLandPlaysOrActivatedAbilities() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        Permanent opposingOriss = addCreatureReady(player2, new OrissSamiteGuardian());
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DakmorSalvage()));
        harness.playLand(player2, 0);
        harness.assertOnBattlefield(player2, "Dakmor Salvage");
        harness.activateAbility(player2, 0, null, opposingOriss.getId());
        harness.passBothPriorities();
        assertThat(opposingOriss.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Grandeur's spell and attack restrictions expire at end of turn")
    void grandeurRestrictionsExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        Permanent creature = addCreatureReady(player2, new NessianCourser());
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .contains(indexOf(player2, creature));
    }

    @Test
    @DisplayName("Grandeur restricts creatures entering after it resolves")
    void grandeurRestrictsCreaturesEnteringLater() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        harness.setHand(player1, List.of(new OrissSamiteGuardian()));
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new TaroxBladewing());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player2.getId()))
                .doesNotContain(indexOf(player2, laterCreature));
    }

    @Test
    @DisplayName("Grandeur does not restrict a creature after the untargeted player gains control")
    void grandeurRestrictionDoesNotFollowCreatureToAnotherController() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new OrissSamiteGuardian());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TaroxBladewing());
        harness.setHand(player1, List.of(new OrissSamiteGuardian(), new TakePossession()));
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, creature));
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
