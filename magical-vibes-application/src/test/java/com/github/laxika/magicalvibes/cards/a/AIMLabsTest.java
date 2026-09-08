package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AIMLabs.class)
class AIMLabsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new AIMLabs()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent labs = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(labs.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent labs = addLabsReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(labs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent labs = addLabsReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(labs.isTapped()).isTrue();
    }

    private Permanent addLabsReady() {
        Permanent labs = new Permanent(new AIMLabs());
        labs.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(labs);
        return labs;
    }
}
