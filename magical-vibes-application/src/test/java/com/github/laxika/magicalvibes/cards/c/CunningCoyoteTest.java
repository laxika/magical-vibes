package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningCoyote.class, ArmoredArmadillo.class})
class CunningCoyoteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control +1/+1 and haste")
    void etbBoostsAndGrantsHaste() {
        Permanent armadillo = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, armadillo.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(armadillo.getPowerModifier()).isEqualTo(1);
        assertThat(armadillo.getToughnessModifier()).isEqualTo(1);
        assertThat(armadillo.getEffectivePower()).isEqualTo(1);
        assertThat(armadillo.getEffectiveToughness()).isEqualTo(5);
        assertThat(armadillo.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB boost and haste wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent armadillo = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, armadillo.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(armadillo.getPowerModifier()).isZero();
        assertThat(armadillo.getToughnessModifier()).isZero();
        assertThat(armadillo.getEffectivePower()).isZero();
        assertThat(armadillo.getEffectiveToughness()).isEqualTo(4);
        assertThat(armadillo.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB targets another Coyote rather than itself")
    void etbTargetsAnotherCoyote() {
        Permanent existingCoyote = harness.addToBattlefieldAndReturn(player1, new CunningCoyote());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, existingCoyote.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(existingCoyote.getPowerModifier()).isEqualTo(1);
        assertThat(existingCoyote.getToughnessModifier()).isEqualTo(1);
        assertThat(existingCoyote.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(existingCoyote.getId()))
                .singleElement()
                .satisfies(coyote -> {
                    assertThat(coyote.getPowerModifier()).isZero();
                    assertThat(coyote.getToughnessModifier()).isZero();
                    assertThat(coyote.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
                });
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent armadillo = harness.addToBattlefieldAndReturn(player2, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, armadillo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    void coyoteAndItsTargetCanAttackImmediately() {
        Permanent armadillo = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        armadillo.setSummoningSick(true);
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, armadillo.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(creature -> assertThat(creature.isAttacking()).isTrue());
    }

    @Test
    void plotExilesWithoutTriggeringAndCastsForFreeOnLaterTurn() {
        Permanent armadillo = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        CunningCoyote coyote = new CunningCoyote();
        harness.setHand(player1, List.of(coyote));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        harness.assertNotInHand(player1, "Cunning Coyote");
        harness.assertNotOnBattlefield(player1, "Cunning Coyote");
        assertThat(gd.plottedCardIds).contains(coyote.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
        assertThat(armadillo.getPowerModifier()).isZero();
        assertThat(armadillo.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        assertThatThrownBy(() -> harness.castFromExile(player1, coyote.getId(), armadillo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("turn it became plotted");

        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, coyote.getId(), armadillo.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cunning Coyote");
        assertThat(armadillo.getPowerModifier()).isEqualTo(1);
        assertThat(armadillo.getToughnessModifier()).isEqualTo(1);
        assertThat(armadillo.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    void plotRequiresFullCostAndSorceryTiming() {
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInHand(player1, "Cunning Coyote");
    }

    @Test
    void targetLeavingBattlefieldPreventsBothEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CunningCoyote());
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(remaining -> {
                    assertThat(remaining.getPowerModifier()).isZero();
                    assertThat(remaining.getToughnessModifier()).isZero();
                });
    }

    @Test
    @DisplayName("Can be cast without a target when no other creatures are controlled")
    void canBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new CunningCoyote()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
