package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SweepingCleave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHandedAxe.class, SweepingCleave.class, GrizzlyBears.class})
class TwoHandedAxeTest extends BaseCardTest {

    @Test
    void equippedCreatureHasItsPowerDoubledWhenItAttacks() {
        Permanent creature = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        creature.setPowerModifier(1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void adventureGrantsDoubleStrikeToCreatureYouControlAndExilesTheCard() {
        TwoHandedAxe card = new TwoHandedAxe();
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetOpponentCreature() {
        TwoHandedAxe card = new TwoHandedAxe();
        Permanent creature = addCreatureReady(player2);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void attackTriggerStillBoostsOriginalAttackerAfterAxeMoves() {
        Permanent attacker = addCreatureReady(player1);
        Permanent other = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(attacker.getId());
        declareAttackers(List.of(0));

        axe.setAttachedTo(other.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    void attackTriggerStillBoostsAttackerAfterAxeBecomesUnattached() {
        Permanent attacker = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(attacker.getId());
        declareAttackers(List.of(0));

        axe.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
    }

    @Test
    void attackTriggerUsesPowerAtResolutionAndDoesNotContinuouslyDoubleIt() {
        Permanent attacker = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(attacker.getId());
        declareAttackers(List.of(0));

        attacker.setPowerModifier(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);

        attacker.setPowerModifier(attacker.getPowerModifier() + 1);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
    }

    @Test
    void attackTriggerDoublesNegativePower() {
        Permanent attacker = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(attacker.getId());
        attacker.setPowerModifier(-3);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    void twoAxesDoublePowerTwice() {
        Permanent attacker = addCreatureReady(player1);
        addAxeReady(player1).setAttachedTo(attacker.getId());
        addAxeReady(player1).setAttachedTo(attacker.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(8);
    }

    @Test
    void equipAttachesAxeWithoutBoostingPower() {
        Permanent creature = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void adventureDoubleStrikeExpiresAndArtifactCanBeCastFromExile() {
        TwoHandedAxe card = new TwoHandedAxe();
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Two-Handed Axe");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(findPermanent(player1, "Two-Handed Axe").getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardInsteadOfExile() {
        TwoHandedAxe card = new TwoHandedAxe();
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void attackTriggerStillResolvesAfterAxeLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1);
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(attacker.getId());
        declareAttackers(List.of(0));

        gd.playerBattlefields.get(player1.getId()).remove(axe);
        gd.playerGraveyards.get(player1.getId()).add(axe.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addAxeReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new TwoHandedAxe());
    }

}
