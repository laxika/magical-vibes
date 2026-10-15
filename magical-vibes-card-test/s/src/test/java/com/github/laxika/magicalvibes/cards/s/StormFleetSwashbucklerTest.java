package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpelFromOrazca;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormFleetSwashbuckler.class, Forest.class, GrizzlyBears.class, ExpelFromOrazca.class, TurnToFrog.class})
class StormFleetSwashbucklerTest extends BaseCardTest {

    @Test
    @DisplayName("It does not have double strike without the city's blessing")
    void noDoubleStrikeWithoutBlessing() {
        Permanent swashbuckler = addCreatureReady(player1, new StormFleetSwashbuckler());

        assertThat(gqs.hasKeyword(gd, swashbuckler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Permanent ascend grants the city's blessing when the tenth permanent enters")
    void gainsDoubleStrikeWhenTenthPermanentEnters() {
        Permanent swashbuckler = harness.addToBattlefieldAndReturn(player1, new StormFleetSwashbuckler());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.hasKeyword(gd, swashbuckler, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, swashbuckler, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Casting it as the tenth permanent gives its controller the city's blessing")
    void ascendsWhenItEntersAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.castFromHand(player1, new StormFleetSwashbuckler(), "{1}{R}");
        harness.passBothPriorities();

        Permanent swashbuckler = findPermanent(player1, "Storm Fleet Swashbuckler");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, swashbuckler, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The opponent's permanents do not count toward ascend or grant double strike")
    void opponentsPermanentsDoNotGrantBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.enterBattlefieldAndReturn(player2, new StormFleetSwashbuckler());
        Permanent swashbuckler = harness.enterBattlefieldAndReturn(player1, new StormFleetSwashbuckler());

        assertThat(gd.playersWithCityBlessing).contains(player2.getId()).doesNotContain(player1.getId());
        assertThat(gqs.hasKeyword(gd, swashbuckler, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike remains after dropping below ten permanents")
    void retainsDoubleStrikeBelowTenPermanents() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent swashbuckler = harness.enterBattlefieldAndReturn(player1, new StormFleetSwashbuckler());
        Permanent other = harness.enterBattlefieldAndReturn(player1, new StormFleetSwashbuckler());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        harness.setHand(player2, List.of(new ExpelFromOrazca()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, other.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(9);
        harness.assertInHand(player1, "Storm Fleet Swashbuckler");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, swashbuckler, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("With the city's blessing an unblocked attack deals damage twice")
    void dealsDoubleStrikeCombatDamageWithBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent swashbuckler = harness.enterBattlefieldAndReturn(player1, new StormFleetSwashbuckler());
        swashbuckler.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(List.of(9));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Without the city's blessing an unblocked attack deals damage once")
    void dealsRegularCombatDamageWithoutBlessing() {
        addCreatureReady(player1, new StormFleetSwashbuckler());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Losing ascend before the tenth permanent enters prevents gaining the city's blessing")
    void cannotAscendWhileAbilitiesAreRemoved() {
        Permanent swashbuckler = harness.enterBattlefieldAndReturn(player1, new StormFleetSwashbuckler());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, swashbuckler.getId());

        assertThat(gqs.hasLostAllAbilities(gd, swashbuckler)).isTrue();
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(10);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }
}
