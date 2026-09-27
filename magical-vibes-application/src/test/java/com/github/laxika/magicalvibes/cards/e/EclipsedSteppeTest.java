package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({EclipsedSteppe.class, Forest.class, Mountain.class, EvolvingWilds.class})
class EclipsedSteppeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without two basic lands")
    void entersTappedWithoutTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new EvolvingWilds());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control two basic lands")
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's basic lands do not satisfy the check")
    void opponentBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Produces white mana")
    void producesWhiteMana() {
        addSteppeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Produces black mana")
    void producesBlackMana() {
        addSteppeReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playSteppe() {
        harness.setHand(player1, List.of(new EclipsedSteppe()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addSteppeReady(Player player) {
        Permanent steppe = new Permanent(new EclipsedSteppe());
        steppe.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(steppe);
        return steppe;
    }

    private Permanent findSteppe(Player player) {
        return findPermanent(player, "Eclipsed Steppe");
    }
}
