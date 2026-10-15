package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarrenGlory;
import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatriciansScorn.class, BarrenGlory.class, BladeOfTheSixthPride.class, FomoriNomad.class})
class PatriciansScornTest extends BaseCardTest {

    @Test
    void castsForFreeAfterCastingAnotherWhiteSpell() {
        BladeOfTheSixthPride whiteSpell = new BladeOfTheSixthPride();
        PatriciansScorn scorn = new PatriciansScorn();
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new BarrenGlory());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new BarrenGlory());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FomoriNomad());
        harness.castFromHand(player1, whiteSpell, "{1}{W}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(scorn));
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentEnchantment)
                .contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorn);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void castsNormallyWithoutAnotherWhiteSpell() {
        PatriciansScorn scorn = new PatriciansScorn();
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new BarrenGlory());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new BarrenGlory());
        harness.castFromHand(player1, scorn, "{3}{W}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorn);
    }

    @Test
    void cannotUseFreeAlternateCostAfterCastingNonWhiteSpell() {
        FomoriNomad nonWhiteSpell = new FomoriNomad();
        PatriciansScorn scorn = new PatriciansScorn();
        harness.castFromHand(player1, nonWhiteSpell, "{4}{R}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(scorn));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseFreeAlternateCostWithoutAnotherWhiteSpell() {
        PatriciansScorn scorn = new PatriciansScorn();
        harness.setHand(player1, List.of(scorn));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castsForFreeBeforeTheWhiteSpellResolves() {
        BladeOfTheSixthPride whiteSpell = new BladeOfTheSixthPride();
        PatriciansScorn scorn = new PatriciansScorn();
        BarrenGlory enchantment = new BarrenGlory();
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, enchantment);
        harness.castFromHand(player1, whiteSpell, "{1}{W}");
        harness.setHand(player1, List.of(scorn));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(permanent);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorn);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == whiteSpell);
    }

    @Test
    void aNormallyCastScornEnablesAnotherScornForFree() {
        PatriciansScorn first = new PatriciansScorn();
        PatriciansScorn second = new PatriciansScorn();
        harness.castFromHand(player1, first, "{3}{W}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(second));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsWhiteSpellDoesNotEnableFreeCasting() {
        harness.castFromHand(player2, new PatriciansScorn(), "{3}{W}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PatriciansScorn()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
