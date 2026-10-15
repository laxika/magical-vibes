package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EumidianTerrabotanist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheDominionBracelet.class, GrizzlyBears.class, EumidianTerrabotanist.class})
class TheDominionBraceletTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bracelet = addCreatureReady(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void abilityUsesEquippedPowerForReductionAndExilesBracelet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bracelet = addCreatureReady(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
        assertThat(gd.findExiledCard(bracelet.getCard().getId())).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void abilityCanTargetOnlyAnOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bracelet = addCreatureReady(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipAttachesToChosenCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        Permanent bracelet = harness.addToBattlefieldAndReturn(player1, new TheDominionBracelet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bracelet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void insufficientManaLeavesCostsUnpaid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        Permanent bracelet = harness.addToBattlefieldAndReturn(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bracelet);
        assertThat(gd.findExiledCard(bracelet.getCard().getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(11);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedAbilityRequiresSorceryTiming() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        Permanent bracelet = harness.addToBattlefieldAndReturn(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 15);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, 0, null, creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bracelet);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(14);
    }

    @Test
    void highPowerReducesManaCostToZeroAndExileIsPaidBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        creature.setPowerModifier(20);
        Permanent bracelet = harness.addToBattlefieldAndReturn(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(gd.findExiledCard(bracelet.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bracelet);
        assertThat(gd.pendingTurnControl).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(22);
        harness.passBothPriorities();
        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    void creatureControllerCanExileBraceletControlledByOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        Permanent bracelet = harness.addToBattlefieldAndReturn(player2, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bracelet.getCard().getId())).isNotNull();
        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    void controlLastsOnlyForOpponentsNextTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        Permanent bracelet = harness.addToBattlefieldAndReturn(player1, new TheDominionBracelet());
        bracelet.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.mindControlledPlayerId).isNull();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.pendingTurnControl).isEmpty();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.mindControllerPlayerId).isNull();
    }

}
