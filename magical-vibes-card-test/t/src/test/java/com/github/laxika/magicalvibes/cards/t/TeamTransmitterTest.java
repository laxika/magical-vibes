package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeamTransmitter.class, LukeCagePowerMan.class, GrizzlyBears.class})
class TeamTransmitterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when a Hero enters under your control")
    void heroEnteringGainsLife() {
        harness.addToBattlefield(player1, new TeamTransmitter());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new LukeCagePowerMan(), "{3}{W}");
        resolveCreatureAndTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Hero creature")
    void nonHeroEnteringDoesNotGainLife() {
        harness.addToBattlefield(player1, new TeamTransmitter());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
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

    @Test
    @DisplayName("An opposing Hero entering does not gain life")
    void opposingHeroDoesNotGainLife() {
        harness.addToBattlefield(player1, new TeamTransmitter());
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.enterBattlefieldAndReturn(player2, new LukeCagePowerMan());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Each transmitter triggers for a Hero entering without being cast")
    void eachTransmitterTriggersForHeroPutOntoBattlefield() {
        harness.addToBattlefield(player1, new TeamTransmitter());
        harness.addToBattlefield(player1, new TeamTransmitter());
        int lifeBefore = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new LukeCagePowerMan());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The mana ability resolves immediately and produces exactly one chosen mana")
    void manaAbilitySupportsEveryColor(ManaColor color) {
        Permanent transmitter = harness.addToBattlefieldAndReturn(player1, new TeamTransmitter());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        for (ManaColor other : ManaColor.values()) {
            if (other != color) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(other)).isZero();
            }
        }
        assertThat(transmitter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
