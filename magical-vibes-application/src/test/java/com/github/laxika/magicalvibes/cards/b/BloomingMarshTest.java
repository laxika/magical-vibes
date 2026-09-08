package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloomingMarshTest extends BaseCardTest {

    @Test
    void entersUntappedWithTwoOtherLands() {
        addMountain(player1);
        addMountain(player1);

        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isFalse();
    }

    @Test
    void entersTappedWithThreeOtherLands() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isTrue();
    }

    @Test
    void nonLandPermanentsDoNotCount() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));

        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isFalse();
    }

    @Test
    void opponentsLandsDoNotCount() {
        addMountain(player2);
        addMountain(player2);
        addMountain(player2);

        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isFalse();
    }

    @Test
    void tappingProducesBlackMana() {
        addReadyMarsh(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void tappingProducesGreenMana() {
        addReadyMarsh(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void castBloomingMarsh() {
        harness.setHand(player1, List.of(new BloomingMarsh()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyMarsh(Player player) {
        Permanent permanent = new Permanent(new BloomingMarsh());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addMountain(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Mountain()));
    }

    private Permanent findMarsh(Player player) {
        return findPermanent(player, "Blooming Marsh");
    }
}
