package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloomingMarsh.class, Mountain.class, KujarSeedsculptor.class})
class BloomingMarshTest extends BaseCardTest {

    @Test
    void entersUntappedWithNoOtherLands() {
        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isFalse();
    }

    @Test
    void tappedLandsStillCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        }

        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedAsThirdLandDespiteOtherPermanentsAndOpponentsLands() {
        addMountain(player1);
        addMountain(player1);
        harness.addToBattlefield(player1, new KujarSeedsculptor());
        for (int i = 0; i < 3; i++) {
            addMountain(player2);
        }

        castBloomingMarsh();

        assertThat(findMarsh(player1).isTapped()).isFalse();
    }

    @Test
    void enteringWithoutBeingPlayedStillChecksOtherLands() {
        for (int i = 0; i < 3; i++) {
            addMountain(player1);
        }

        Permanent marsh = harness.enterBattlefieldAndReturn(player1, new BloomingMarsh());

        assertThat(marsh.isTapped()).isTrue();
    }

    @Test
    void canProduceEitherColorImmediatelyAfterEnteringUntapped() {
        castBloomingMarsh();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findMarsh(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findMarsh(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

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
        harness.addToBattlefield(player1, new KujarSeedsculptor());
        harness.addToBattlefield(player1, new KujarSeedsculptor());
        harness.addToBattlefield(player1, new KujarSeedsculptor());

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
        return addCreatureReady(player, new BloomingMarsh());
    }

    private void addMountain(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findMarsh(Player player) {
        return findPermanent(player, "Blooming Marsh");
    }
}
