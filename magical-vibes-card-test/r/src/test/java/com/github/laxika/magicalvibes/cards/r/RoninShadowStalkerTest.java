package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoninShadowStalker.class, VulshokMorningstar.class, CopperMyr.class, GrizzlyBears.class})
class RoninShadowStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life for two mana of one color and can do so only once each turn")
    void paysLifeForRestrictedManaOnceEachTurn() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana casts an Equipment spell")
    void restrictedManaCastsEquipment() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.setHand(player1, List.of(new VulshokMorningstar()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a non-Equipment artifact")
    void restrictedManaCannotCastNonEquipment() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.setHand(player1, List.of(new CopperMyr()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Restricted mana pays for an equip ability")
    void restrictedManaPaysForEquipAbility() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.activateAbility(player1, battlefieldIndex(equipment), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing an attached Equipment gives a creature -4/-4")
    void sacrificesAttachedEquipmentToGiveCreatureMinusFourMinusFour() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void manaAbilityWorksWhileSummoningSickOnOpponentsTurn() {
        Permanent ronin = harness.addToBattlefieldAndReturn(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.assertLife(player1, 18);
        assertThat(ronin.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayLifeCostWithOnlyOneLife() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 1);
    }

    @Test
    void manaAbilityCanBeActivatedAgainOnNextPlayersTurn() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.assertLife(player1, 16);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSacrificeUnattachedEquipment() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    @Test
    void cannotSacrificeEquipmentAttachedToAnotherCreature() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(other.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    @Test
    void cannotSacrificeOpponentsEquipmentAttachedToRonin() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    @Test
    void sacrificeAbilityCannotBeActivatedDuringCombat() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    @Test
    void sacrificeAbilityCannotRespondToSpell() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VulshokMorningstar(), "{2}");

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndPenaltyExpiresAtEndOfTurn() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        sacrificed.setAttachedTo(ronin.getId());
        retained.setAttachedTo(ronin.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId());
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed).contains(retained);
        harness.assertInGraveyard(player1, "Vulshok Morningstar");
        assertThat(ronin.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(5);
    }

    @Test
    void sacrificeAbilityRequiresRoninToBeReadyToTap() {
        Permanent ronin = harness.addToBattlefieldAndReturn(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    @Test
    void sacrificeAbilityCannotBeActivatedOnOpponentsTurn() {
        Permanent ronin = addCreatureReady(player1, new RoninShadowStalker());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(ronin.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ronin), 1, null, ronin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(ronin.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
