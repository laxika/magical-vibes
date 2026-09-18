package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VernalFen.class, Forest.class})
class VernalFenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control fewer than two basic lands")
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());

        playLand();

        assertThat(findFen().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control two basic lands")
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        playLand();

        assertThat(findFen().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nonbasic lands do not satisfy the basic-land check")
    void nonbasicLandsDoNotSatisfyCheck() {
        harness.addToBattlefield(player1, new VernalFen());
        harness.addToBattlefield(player1, new VernalFen());

        playLand();

        assertThat(findFen().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent fen = addReadyFen();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(fen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent fen = addReadyFen();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(fen.isTapped()).isTrue();
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
