package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HauntingApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Power is 1 with empty graveyards; toughness stays 2")
    void powerIsOneWithEmptyGraveyards() {
        Permanent apparition = addApparitionReady(player1);

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apparition)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power is 1 plus green creature cards in the chosen opponent's graveyard")
    void powerCountsOpponentGreenCreatures() {
        Permanent apparition = addApparitionReady(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LlanowarElves()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, apparition)).isEqualTo(2);
    }

    @Test
    @DisplayName("Green noncreature cards and nongreen creature cards do not count")
    void onlyGreenCreatureCardsCount() {
        Permanent apparition = addApparitionReady(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GiantGrowth(), new HillGiant()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(2);
    }

    @Test
    @DisplayName("Green creature cards in the controller's own graveyard do not count")
    void ownGraveyardDoesNotCount() {
        Permanent apparition = addApparitionReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates as green creature cards enter the opponent's graveyard")
    void powerUpdatesDynamically() {
        Permanent apparition = addApparitionReady(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(2);

        gd.playerGraveyards.get(player2.getId()).add(new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, apparition)).isEqualTo(3);
    }

    private Permanent addApparitionReady(Player player) {
        Permanent permanent = new Permanent(new HauntingApparition());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
