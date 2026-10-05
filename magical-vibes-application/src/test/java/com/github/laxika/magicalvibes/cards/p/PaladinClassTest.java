package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaladinClass.class, Opt.class, SteadfastPaladin.class})
class PaladinClassTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents' spells cost more during your turn")
    void taxesOpponentsSpellsDuringYourTurn() {
        harness.addToBattlefield(player1, new PaladinClass());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        prepareForSorcery(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Level two boosts creatures you control")
    void levelTwoBoostsCreaturesYouControl() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent creature = addReadyCreature(player1);

        levelUpToTwo(paladinClass);

        assertThat(paladinClass.getClassLevel()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Level three boosts one attacking creature for each other attacker and grants double strike")
    void levelThreeBoostsForOtherAttackersAndGrantsDoubleStrike() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent target = addReadyCreature(player1);
        Permanent otherAttacker = addReadyCreature(player1);
        Permanent anotherAttacker = addReadyCreature(player1);

        levelUpToTwo(paladinClass);
        levelUpToThree(paladinClass);

        int powerBeforeAttack = gqs.getEffectivePower(gd, target);
        int toughnessBeforeAttack = gqs.getEffectiveToughness(gd, target);
        declareAttackers(List.of(1, 2, 3));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBeforeAttack + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBeforeAttack + 2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, anotherAttacker)).isEqualTo(3);
    }

    @Test
    void opponentCanCastForNormalCostOnTheirOwnTurn() {
        harness.addToBattlefield(player1, new PaladinClass());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        prepareForSorcery(player2);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void controllerDoesNotPayTaxOnTheirOwnTurn() {
        harness.addToBattlefield(player1, new PaladinClass());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        prepareForSorcery(player1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentCanCastWhenTheyPayTheAdditionalMana() {
        harness.addToBattlefield(player1, new PaladinClass());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        prepareForSorcery(player1);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void multipleClassesAddTheirSpellTaxes() {
        harness.addToBattlefield(player1, new PaladinClass());
        harness.addToBattlefield(player1, new PaladinClass());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        prepareForSorcery(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void levelTwoDoesNotBoostOpponentsCreaturesOrTriggerWhenAttacking() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        int opponentPower = gqs.getEffectivePower(gd, opponentCreature);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponentCreature);
        levelUpToTwo(paladinClass);

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(opponentToughness);
        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void loneAttackerGainsDoubleStrikeWithoutAdditionalBoost() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent attacker = addReadyCreature(player1);
        levelUpToTwo(paladinClass);
        levelUpToThree(paladinClass);
        int power = gqs.getEffectivePower(gd, attacker);
        int toughness = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void countsOtherAttackersAtResolution() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent attacker = addReadyCreature(player1);
        Permanent other = addReadyCreature(player1);
        levelUpToTwo(paladinClass);
        levelUpToThree(paladinClass);
        int power = gqs.getEffectivePower(gd, attacker);

        declareAttackers(List.of(1, 2));
        harness.handlePermanentChosen(player1, attacker.getId());
        other.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(power);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void attackTriggerResolvesAfterClassLeavesBattlefield() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent attacker = addReadyCreature(player1);
        addReadyCreature(player1);
        int basePower = gqs.getEffectivePower(gd, attacker);
        levelUpToTwo(paladinClass);
        levelUpToThree(paladinClass);

        declareAttackers(List.of(1, 2));
        harness.handlePermanentChosen(player1, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(paladinClass);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(basePower + 1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void cannotSkipAClassLevelOrRepeatLevelTwo() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.WHITE, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(paladinClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        levelUpToTwo(paladinClass);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(paladinClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelingRequiresSorceryTiming() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        prepareForSorcery(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(paladinClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paladinClass.getClassLevel()).isEqualTo(1);
    }

    private void levelUpToTwo(Permanent paladinClass) {
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, battlefieldIndex(paladinClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent paladinClass) {
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, battlefieldIndex(paladinClass), 1, null, null);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareForSorcery(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new SteadfastPaladin());
    }
}
