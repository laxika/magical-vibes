package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggSquad.class, CursedScroll.class})
class MoggSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Mogg Squad is 3/3 when it is the only creature")
    void baseStatsAlone() {
        Permanent squad = addMoggSquad(player1);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mogg Squad shrinks for each other creature you control")
    void shrinksForOwnCreatures() {
        Permanent squad = addMoggSquad(player1);
        addMoggSquad(player1);
        addMoggSquad(player1);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mogg Squad shrinks for creatures any player controls")
    void shrinksForOpponentCreatures() {
        Permanent squad = addMoggSquad(player1);
        addMoggSquad(player2);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another Mogg Squad counts, and each counts the other")
    void twoSquadsCountEachOther() {
        Permanent squad = addMoggSquad(player1);
        Permanent other = addMoggSquad(player2);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mogg Squad grows back when other creatures leave the battlefield")
    void updatesWhenCreaturesLeave() {
        Permanent squad = addMoggSquad(player1);
        Permanent other = addMoggSquad(player1);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mogg Squad ignores noncreature permanents")
    void ignoresNoncreatures() {
        Permanent squad = addMoggSquad(player1);
        harness.addToBattlefield(player1, new CursedScroll());
        harness.addToBattlefield(player2, new CursedScroll());

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(3);
    }

    private Permanent addMoggSquad(Player player) {
        return addCreatureReady(player, new MoggSquad());
    }
}
