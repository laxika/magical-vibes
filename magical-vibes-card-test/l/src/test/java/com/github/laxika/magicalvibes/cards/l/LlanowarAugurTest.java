package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarAugur.class, NessianCourser.class, HorizonCanopy.class})
class LlanowarAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and gives a target creature +3/+3 and trample")
    void sacrificesItselfAndBoostsTarget() {
        addAugur();
        Permanent target = addTargetCreature(player1);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        harness.assertNotOnBattlefield(player1, "Llanowar Augur");
        harness.assertInGraveyard(player1, "Llanowar Augur");
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        addAugur();
        Permanent target = addTargetCreature(player1);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        addAugur();
        Permanent target = addTargetCreature(player2);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Can activate without paying mana")
    void canActivateWithoutPayingMana() {
        addAugur();
        Permanent target = addTargetCreature(player1);
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot activate the ability outside your upkeep")
    void cannotActivateOutsideYourUpkeep() {
        addAugur();
        Permanent target = addTargetCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addAugur();
        Permanent horizonCanopy = harness.addToBattlefieldAndReturn(player2, new HorizonCanopy());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, horizonCanopy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addAugur();
        Permanent target = addTargetCreature(player1);
        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");

        harness.assertOnBattlefield(player1, "Llanowar Augur");
        harness.assertNotInGraveyard(player1, "Llanowar Augur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately while the boost waits for resolution")
    void sacrificeIsPaidBeforeResolution() {
        addAugur();
        Permanent target = addTargetCreature(player1);
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Llanowar Augur");
        harness.assertInGraveyard(player1, "Llanowar Augur");
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Can target itself, but the ability has no legal target after sacrifice")
    void canTargetItselfAndSacrificeWithoutBoostingOtherCreatures() {
        Permanent augur = addAugur();
        Permanent otherCreature = addTargetCreature(player1);
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, augur.getId());

        harness.assertInGraveyard(player1, "Llanowar Augur");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Llanowar Augur");
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
        assertThat(otherCreature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    private Permanent addAugur() {
        return harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
    }

    private Permanent addTargetCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new NessianCourser());
    }
}
