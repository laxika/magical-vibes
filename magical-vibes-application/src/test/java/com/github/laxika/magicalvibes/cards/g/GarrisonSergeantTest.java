package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrisonSergeant.class, BorosGuildgate.class})
class GarrisonSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Has double strike while its controller controls a Gate")
    void hasDoubleStrikeWithGate() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        harness.addToBattlefield(player1, new BorosGuildgate());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have double strike without a Gate")
    void noDoubleStrikeWithoutGate() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Gate does not grant double strike")
    void opponentGateDoesNotCount() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        harness.addToBattlefield(player2, new BorosGuildgate());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when the Gate leaves the battlefield")
    void losesDoubleStrikeWhenGateLeaves() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(gate);

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike immediately when a Gate enters, even tapped")
    void gainsDoubleStrikeWhenGateEnters() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isFalse();

        Permanent gate = harness.enterBattlefieldAndReturn(player1, new BorosGuildgate());

        assertThat(gate.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Keeps double strike while at least one Gate remains")
    void keepsDoubleStrikeWithAnotherGate() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new GarrisonSergeant());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());

        gd.playerBattlefields.get(player1.getId()).remove(gate);

        assertThat(gqs.hasKeyword(gd, sergeant, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Deals damage in both combat damage steps while controlling a Gate")
    void dealsDoubleStrikeCombatDamageWithGate() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GarrisonSergeant());
        harness.addToBattlefield(player1, new BorosGuildgate());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Deals damage only once without a Gate")
    void dealsNormalCombatDamageWithoutGate() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GarrisonSergeant());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
