package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

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

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    @DisplayName("Squads with zero or negative toughness all die simultaneously")
    void squadsDieSimultaneously(int creatureCount) {
        Permanent squad = addMoggSquad(player1);
        for (int i = 1; i < creatureCount; i++) {
            addMoggSquad(i % 2 == 0 ? player1 : player2);
        }

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(4 - creatureCount);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(4 - creatureCount);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mogg Squad");
        harness.assertNotOnBattlefield(player2, "Mogg Squad");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize((creatureCount + 1) / 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(creatureCount / 2);
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures still count")
    void countsTappedAndSummoningSickCreatures() {
        Permanent squad = addMoggSquad(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player2, new MoggSquad());
        other.setTapped(true);
        other.setSummoningSick(true);

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature cards outside the battlefield do not count")
    void ignoresCreaturesInHandAndGraveyard() {
        Permanent squad = addMoggSquad(player1);
        harness.setHand(player1, List.of(new MoggSquad()));
        harness.setHand(player2, List.of(new MoggSquad()));
        harness.setGraveyard(player1, List.of(new MoggSquad()));
        harness.setGraveyard(player2, List.of(new MoggSquad()));

        assertThat(gqs.getEffectivePower(gd, squad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squad)).isEqualTo(3);
    }

    private Permanent addMoggSquad(Player player) {
        return addCreatureReady(player, new MoggSquad());
    }
}
