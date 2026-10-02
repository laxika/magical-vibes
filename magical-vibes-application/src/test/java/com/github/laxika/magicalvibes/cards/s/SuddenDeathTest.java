package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.cards.n.NantukoShaman;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenDeath.class, DurkwoodBaloth.class, NantukoShaman.class, PrismaticLens.class,
        SuddenShock.class})
class SuddenDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-4 until end of turn")
    void givesTargetCreatureMinusFourMinusFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBaloth());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    @DisplayName("Kills a creature whose toughness is reduced to zero")
    void killsCreatureWithZeroToughness() {
        harness.addToBattlefield(player2, new NantukoShaman());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(
                player1, 0, harness.getPermanentId(player2, "Nantuko Shaman"));

        harness.assertNotOnBattlefield(player2, "Nantuko Shaman");
        harness.assertInGraveyard(player2, "Nantuko Shaman");
    }

    @Test
    @DisplayName("The reduction wears off at end of turn")
    void reductionWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBaloth());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Split second still allows mana abilities")
    void splitSecondStillAllowsManaAbilities() {
        Permanent lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NantukoShaman());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(lens.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Split second prevents a spell response")
    void splitSecondPreventsSpellResponse() {
        harness.addToBattlefield(player2, new NantukoShaman());
        harness.setHand(player1, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Nantuko Shaman"));

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
