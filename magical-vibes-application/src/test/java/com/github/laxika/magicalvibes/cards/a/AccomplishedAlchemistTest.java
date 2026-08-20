package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccomplishedAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one mana of the chosen color")
    void firstAbilityAddsOneMana() {
        Permanent alchemist = addReadyAlchemist();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(alchemist.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds mana equal to life gained this turn")
    void secondAbilityAddsManaEqualToLifeGained() {
        Permanent alchemist = addReadyAlchemist();
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(alchemist.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second ability counts only life gained by its controller")
    void secondAbilityIgnoresOpponentsLifeGained() {
        Permanent alchemist = addReadyAlchemist();
        gd.lifeGainedThisTurn.put(player2.getId(), 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent addReadyAlchemist() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AccomplishedAlchemist());
        alchemist.setSummoningSick(false);
        return alchemist;
    }
}
