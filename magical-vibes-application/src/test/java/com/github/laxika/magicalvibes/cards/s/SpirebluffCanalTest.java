package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InventorsApprentice;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpirebluffCanal.class, Mountain.class, InventorsApprentice.class})
class SpirebluffCanalTest extends BaseCardTest {

    @Test
    void entersUntappedWithTwoOtherLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new SpirebluffCanal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCanal(player1).isTapped()).isFalse();
    }

    @Test
    void entersTappedWithThreeOtherLands() {
        addBasicLand(player1);
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new SpirebluffCanal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCanal(player1).isTapped()).isTrue();
    }

    @Test
    void opponentLandsDoNotCount() {
        addBasicLand(player2);
        addBasicLand(player2);
        addBasicLand(player2);

        harness.setHand(player1, List.of(new SpirebluffCanal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCanal(player1).isTapped()).isFalse();
    }

    @Test
    void producesBlueMana() {
        addCanalReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void producesRedMana() {
        addCanalReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void nonlandPermanentsDoNotCount() {
        addBasicLand(player1);
        addBasicLand(player1);
        harness.addToBattlefield(player1, new InventorsApprentice());
        harness.addToBattlefield(player1, new InventorsApprentice());
        harness.setHand(player1, List.of(new SpirebluffCanal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCanal(player1).isTapped()).isFalse();
    }

    private Permanent addCanalReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SpirebluffCanal());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findCanal(Player player) {
        return findPermanent(player, "Spirebluff Canal");
    }
}
