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
        Permanent fen = new Permanent(new VernalFen());
        fen.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(fen);
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

        playLand();

        assertThat(findFen().isTapped()).isTrue();
    }

    private void playLand() {
        harness.setHand(player1, List.of(new VernalFen()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyFen() {
        Permanent fen = new Permanent(new VernalFen());
        fen.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fen);
        return fen;
    }

    private Permanent findFen() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof VernalFen)
                .reduce((first, second) -> second)
                .orElseThrow();
    }
}
