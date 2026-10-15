package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparasAdjudicators.class, CivilServant.class, Island.class})
class SparasAdjudicatorsTest extends BaseCardTest {

    @Test
    void entersAndStopsAnOpponentsCreatureFromAttackingOrBlocking() {
        harness.addToBattlefield(player2, new CivilServant());
        Permanent bears = castAdjudicators();

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    void handAbilityExilesTheCardAndGrantsOnlyGreenWhiteOrBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setHand(player1, List.of(adjudicators));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(adjudicators.getId())).isNotNull();

        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("GREEN", "WHITE", "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void landGrantEndsWhenSparasAdjudicatorsIsCastFromExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CivilServant());
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setHand(player1, List.of(adjudicators));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        land.untap();

        harness.castFromExile(player1, adjudicators.getId(), target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWhenExiledWithoutResolvingItsHandAbility() {
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setExile(player1, List.of(adjudicators));
        addCastingMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, adjudicators.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(adjudicators.getId())).isNotNull();
    }

    @Test
    void illegalLandTargetPreventsTheExileCastingPermission() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setHand(player1, List.of(adjudicators));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();
        addCastingMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, adjudicators.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(adjudicators.getId())).isNotNull();
    }

    @Test
    void canCastAfterTheGrantedLandLeavesFollowingResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setHand(player1, List.of(adjudicators));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        addCastingMana();

        harness.castFromExile(player1, adjudicators.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(adjudicators.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(adjudicators.getId()));
    }

    @Test
    void opponentsLandReceivesTheManaAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SparasAdjudicators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "WHITE");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void handAbilityWorksDuringOpponentsTurnButCreatureCastingStillRequiresNormalTiming() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setHand(player1, List.of(adjudicators));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        addCastingMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, adjudicators.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(adjudicators.getId())).isNotNull();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void manaGrantPersistsAcrossTurnsWithoutCastingTheExiledCard() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SparasAdjudicators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, harness.getPermanentId(player1, "Island"));
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void landKeepsTheManaAbilityWhenTheCardLeavesExileWithoutBeingCast() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        SparasAdjudicators adjudicators = new SparasAdjudicators();
        harness.setHand(player1, List.of(adjudicators));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        gd.removeFromExile(adjudicators.getId());
        harness.setHand(player1, List.of(adjudicators));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void combatRestrictionExpiresAtTheBeginningOfControllersNextTurn() {
        harness.addToBattlefield(player2, new CivilServant());
        Permanent creature = castAdjudicators();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        declareAttack(creature);

        assertThat(creature.isAttacking()).isTrue();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent castAdjudicators() {
        harness.setHand(player1, List.of(new SparasAdjudicators()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID targetId = harness.getPermanentId(player2, "Civil Servant");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Civil Servant"))
                .findFirst()
                .orElseThrow();
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }
}
