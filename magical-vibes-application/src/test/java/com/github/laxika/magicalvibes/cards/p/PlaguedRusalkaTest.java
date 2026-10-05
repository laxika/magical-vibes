package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlaguedRusalka.class, Gristleback.class, GruulSignet.class})
class PlaguedRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives the target creature -1/-1 until end of turn")
    void sacrificesCreatureAndShrinksTarget() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent fodder = addCreatureReady(player1, new Gristleback());
        Permanent target = addCreatureReady(player2, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Gristleback");
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent fodder = addCreatureReady(player1, new Gristleback());
        Permanent target = addCreatureReady(player2, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice itself as the creature cost")
    void canSacrificeItself() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent target = addCreatureReady(player2, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plagued Rusalka");
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent rusalka = harness.addToBattlefieldAndReturn(player1, new PlaguedRusalka());
        rusalka.setTapped(true);
        rusalka.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plagued Rusalka");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target its controller's creature and kill it with zero toughness")
    void canKillOwnCreature() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent target = addCreatureReady(player1, new PlaguedRusalka());
        Permanent fodder = addCreatureReady(player1, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Gristleback");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Plagued Rusalka");
        harness.assertOnBattlefield(player1, "Plagued Rusalka");
    }

    @Test
    @DisplayName("Repeated activations stack and kill a two-toughness creature")
    void repeatedActivationsStack() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent fodder = addCreatureReady(player1, new Gristleback());
        Permanent target = addCreatureReady(player2, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gristleback");
        harness.assertInGraveyard(player1, "Plagued Rusalka");
        harness.assertInGraveyard(player2, "Gristleback");
        harness.assertNotOnBattlefield(player2, "Gristleback");
    }

    @Test
    @DisplayName("May sacrifice the targeted creature without affecting another creature")
    void canSacrificeTargetedCreature() {
        Permanent rusalka = addCreatureReady(player1, new PlaguedRusalka());
        Permanent target = addCreatureReady(player1, new Gristleback());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertInGraveyard(player1, "Gristleback");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plagued Rusalka");
        assertThat(gqs.getEffectivePower(gd, rusalka)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rusalka)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without paying black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new PlaguedRusalka());
        Permanent target = addCreatureReady(player2, new Gristleback());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Plagued Rusalka");
        harness.assertNotInGraveyard(player1, "Plagued Rusalka");
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
