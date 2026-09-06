package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SerpentsPass.class)
class SerpentsPassTest extends BaseCardTest {

    @Test
    @DisplayName("Serpent's Pass enters the battlefield tapped")
    void entersBattlefieldTapped() {
        playPass();

        assertThat(findPermanent(player1, "Serpent's Pass").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Serpent's Pass adds blue mana")
    void addsBlueMana() {
        Permanent pass = addReadyPass(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Serpent's Pass adds black mana")
    void addsBlackMana() {
        Permanent pass = addReadyPass(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(pass.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Serpent's Pass draws a card")
    void sacrificingDrawsCard() {
        addReadyPass(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Serpent's Pass");
        harness.assertInGraveyard(player1, "Serpent's Pass");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    private void playPass() {
        harness.setHand(player1, List.of(new SerpentsPass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
    }

    private Permanent addReadyPass(Player player) {
        Permanent pass = new Permanent(new SerpentsPass());
        pass.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(pass);
        return pass;
    }
}
