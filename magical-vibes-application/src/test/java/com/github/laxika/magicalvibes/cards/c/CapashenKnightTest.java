package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapashenKnight.class, ElvishLookout.class})
class CapashenKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent knight = addCreatureReady(player1, new CapashenKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times for a cumulative boost")
    void canActivateMultipleTimes() {
        Permanent knight = addCreatureReady(player1, new CapashenKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new CapashenKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new CapashenKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("First strike destroys a 1/1 blocker before it can deal combat damage")
    void firstStrikeDealsCombatDamageFirst() {
        Permanent knight = addCreatureReady(player1, new CapashenKnight());
        Permanent blocker = addCreatureReady(player2, new ElvishLookout());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(knight))));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Capashen Knight");
        harness.assertInGraveyard(player2, "Elvish Lookout");
    }
}
