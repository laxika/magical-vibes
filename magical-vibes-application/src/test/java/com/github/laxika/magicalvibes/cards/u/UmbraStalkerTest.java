package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UmbraStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Umbra Stalker is 0/0 with an empty graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addUmbraStalkerReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T equals the black mana symbols in one graveyard card ({5}{B} = 1)")
    void ptEqualsSingleBlackPip() {
        Permanent perm = addUmbraStalkerReady(player1);
        harness.setGraveyard(player1, List.of(new Nightmare()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple black pips in one card count individually ({2}{B}{B} = 2)")
    void countsMultipleBlackPipsInOneCard() {
        Permanent perm = addUmbraStalkerReady(player1);
        harness.setGraveyard(player1, List.of(new Mortivore()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Black pips are summed across all graveyard cards (1 + 2 = 3)")
    void sumsAcrossGraveyardCards() {
        Permanent perm = addUmbraStalkerReady(player1);
        harness.setGraveyard(player1, List.of(new Nightmare(), new Mortivore()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-black cards contribute no black mana symbols")
    void ignoresNonBlackCards() {
        Permanent perm = addUmbraStalkerReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the controller's graveyard is counted, not the opponent's")
    void countsOnlyControllerGraveyard() {
        Permanent perm = addUmbraStalkerReady(player1);
        harness.setGraveyard(player1, List.of(new Nightmare()));
        harness.setGraveyard(player2, List.of(new Mortivore()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when a black card is added to the graveyard")
    void ptUpdatesWhenBlackCardAdded() {
        Permanent perm = addUmbraStalkerReady(player1);
        harness.setGraveyard(player1, List.of(new Nightmare()));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new Mortivore());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    private Permanent addUmbraStalkerReady(Player player) {
        Card card = new UmbraStalker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
