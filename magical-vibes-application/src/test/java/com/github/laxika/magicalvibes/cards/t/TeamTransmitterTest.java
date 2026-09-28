package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeamTransmitter.class, LukeCagePowerMan.class, GrizzlyBears.class})
class TeamTransmitterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when a Hero enters under your control")
    void heroEnteringGainsLife() {
        harness.addToBattlefield(player1, new TeamTransmitter());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new LukeCagePowerMan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveCreatureAndTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Hero creature")
    void nonHeroEnteringDoesNotGainLife() {
        harness.addToBattlefield(player1, new TeamTransmitter());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Adds mana of the chosen color")
    void manaAbilityAddsChosenColor() {
        Permanent transmitter = harness.addToBattlefieldAndReturn(player1, new TeamTransmitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(transmitter.isTapped()).isTrue();
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
