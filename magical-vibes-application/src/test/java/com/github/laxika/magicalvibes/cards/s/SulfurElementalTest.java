package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.ManaTithe;
import com.github.laxika.magicalvibes.cards.p.Pyrohemia;
import com.github.laxika.magicalvibes.cards.r.RecklessWurm;
import com.github.laxika.magicalvibes.cards.w.WhitemaneLion;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({SulfurElemental.class, WhitemaneLion.class, RecklessWurm.class, ManaTithe.class, Pyrohemia.class, ShiftingSky.class})
class SulfurElementalTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures get +1/-1")
    void buffsAndDebuffsWhiteCreatures() {
        harness.addToBattlefield(player1, new SulfurElemental());
        harness.addToBattlefield(player1, new WhitemaneLion());
        harness.addToBattlefield(player2, new WhitemaneLion());

        Permanent whiteCreature = findPermanent(player1, "Whitemane Lion");
        Permanent opponentWhiteCreature = findPermanent(player2, "Whitemane Lion");

        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentWhiteCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentWhiteCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void ignoresNonwhiteCreatures() {
        harness.addToBattlefield(player1, new SulfurElemental());
        harness.addToBattlefield(player2, new RecklessWurm());

        Permanent nonwhiteCreature = findPermanent(player2, "Reckless Wurm");

        assertThat(gqs.getEffectivePower(gd, nonwhiteCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nonwhiteCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be cast during the opponent's turn")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SulfurElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.passPriority(player2);
        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Split second prevents an opponent's spell response")
    void splitSecondPreventsSpellResponse() {
        SulfurElemental sulfurElemental = new SulfurElemental();
        harness.setHand(player1, List.of(sulfurElemental));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new ManaTithe()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, sulfurElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Split second prevents nonmana activated abilities")
    void splitSecondPreventsNonmanaAbilities() {
        harness.addToBattlefield(player2, new Pyrohemia());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new SulfurElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");
    }

    @Test
    @DisplayName("The static effect starts only when Sulfur Elemental resolves")
    void staticEffectStartsOnResolution() {
        Permanent lion = harness.addToBattlefieldAndReturn(player2, new WhitemaneLion());
        harness.setHand(player1, List.of(new SulfurElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sulfur Elemental");
        assertThat(gqs.getEffectivePower(gd, lion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Sulfur Elementals send white creatures with zero toughness to the graveyard")
    void multipleElementalsReduceToughnessToZero() {
        harness.addToBattlefield(player1, new SulfurElemental());
        harness.addToBattlefield(player2, new SulfurElemental());
        harness.addToBattlefield(player1, new WhitemaneLion());
        harness.addToBattlefield(player2, new WhitemaneLion());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Whitemane Lion");
        harness.assertNotOnBattlefield(player2, "Whitemane Lion");
        harness.assertInGraveyard(player1, "Whitemane Lion");
        harness.assertInGraveyard(player2, "Whitemane Lion");
        harness.assertOnBattlefield(player1, "Sulfur Elemental");
        harness.assertOnBattlefield(player2, "Sulfur Elemental");
    }

    @Test
    @DisplayName("Sulfur Elemental applies its own static effect when it becomes white")
    void affectsItselfWhenWhite() {
        Permanent sky = harness.addToBattlefieldAndReturn(player1, new ShiftingSky());
        sky.setChosenColor(CardColor.WHITE);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new SulfurElemental());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
    }
}
