package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.e.EbonStronghold;
import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenLyre.class, IcatianPhalanx.class, EbonStronghold.class})
class ElvenLyreTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Elven Lyre sacrifices it and boosts the target creature")
    void sacrificesAndBoostsTargetCreature() {
        harness.addToBattlefield(player1, new ElvenLyre());
        Permanent phalanx = addCreatureReady(player1, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, phalanx.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elven Lyre");
        harness.assertInGraveyard(player1, "Elven Lyre");
        assertThat(gqs.getEffectivePower(gd, phalanx)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phalanx)).isEqualTo(6);
    }

    @Test
    @DisplayName("Elven Lyre's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvenLyre());
        Permanent phalanx = addCreatureReady(player1, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, phalanx.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, phalanx)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phalanx)).isEqualTo(6);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phalanx)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phalanx)).isEqualTo(4);
    }

    @Test
    @DisplayName("Elven Lyre can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new ElvenLyre());
        Permanent phalanx = addCreatureReady(player2, new IcatianPhalanx());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, phalanx.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phalanx)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phalanx)).isEqualTo(6);
    }

    @Test
    @DisplayName("Elven Lyre cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new ElvenLyre());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player2, new EbonStronghold());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, stronghold.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elven Lyre cannot be activated without its generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ElvenLyre());
        Permanent phalanx = addCreatureReady(player1, new IcatianPhalanx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, phalanx.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Elven Lyre");
    }

    @Test
    @DisplayName("Elven Lyre cannot be activated while tapped")
    void cannotActivateWhenTapped() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new ElvenLyre());
        Permanent phalanx = addCreatureReady(player1, new IcatianPhalanx());
        lyre.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, phalanx.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
