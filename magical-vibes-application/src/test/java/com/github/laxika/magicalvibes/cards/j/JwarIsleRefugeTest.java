package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent refuge = new Permanent(new JwarIsleRefuge());
        refuge.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(refuge);
        return refuge;
    }
}
