package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VernalFen.class, Forest.class, DrownedCatacomb.class})
class VernalFenTest extends BaseCardTest {

    @Test
    void entersTappedWithoutBasicLands() {
        playVernalFen(player1);

        assertThat(findVernalFen(player1).isTapped()).isTrue();
    }

    @Test
    void oneBasicLandAndOneNonbasicForestStillEnterTapped() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new VernalFen());

        playVernalFen(player1);

        assertThat(findPermanents(player1, "Vernal Fen").getLast().isTapped()).isTrue();
    }

    @Test
    void tappedBasicLandsStillAllowUntappedEntry() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        playVernalFen(player1);

        assertThat(findVernalFen(player1).isTapped()).isFalse();
    }

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());

        playVernalFen(player1);

        assertThat(findVernalFen(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        playVernalFen(player1);

        assertThat(findVernalFen(player1).isTapped()).isFalse();
    }

    @Test
    void onlyControlledBasicLandsCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        playVernalFen(player1);

        assertThat(findVernalFen(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlackMana() {
        Permanent fen = addReadyVernalFen(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(fen.isTapped()).isTrue();
    }

    @Test
    void tappingProducesGreenMana() {
        Permanent fen = addReadyVernalFen(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(fen.isTapped()).isTrue();
    }

    private void playVernalFen(Player player) {
        harness.setHand(player, List.of(new VernalFen()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addReadyVernalFen(Player player) {
        Permanent fen = harness.addToBattlefieldAndReturn(player, new VernalFen());
        fen.setSummoningSick(false);
        return fen;
    }

    private Permanent findVernalFen(Player player) {
        return findPermanent(player, "Vernal Fen");
    }

    @Test
    @DisplayName("Nonbasic lands do not satisfy the basic-land check")
    void nonbasicLandsDoNotSatisfyCheck() {
        harness.addToBattlefield(player1, new VernalFen());
        harness.addToBattlefield(player1, new VernalFen());

        playVernalFen(player1);

        assertThat(findPermanents(player1, "Vernal Fen").getLast().isTapped()).isTrue();
    }
}
