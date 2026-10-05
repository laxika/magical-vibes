package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JwarIsleRefuge.class})
class JwarIsleRefugeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new JwarIsleRefuge()));

        harness.playLand(player1, 0);

        Permanent refuge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(refuge.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds blue mana when blue is chosen")
    void manaAbilityAddsBlueMana() {
        Permanent refuge = addReadyRefuge();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(refuge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black mana when black is chosen")
    void manaAbilityAddsBlackMana() {
        Permanent refuge = addReadyRefuge();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(refuge.isTapped()).isTrue();
    }

    private Permanent addReadyRefuge() {
        Permanent refuge = harness.addToBattlefieldAndReturn(player1, new JwarIsleRefuge());
        refuge.setSummoningSick(false);
        return refuge;
    }

    @Test
    @DisplayName("Entering without being played taps the land and gains life only on trigger resolution")
    void enteringWithoutBeingPlayedTriggersLifeGainForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        Permanent refuge = harness.enterBattlefieldAndReturn(player2, new JwarIsleRefuge());

        assertThat(refuge.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }
}
