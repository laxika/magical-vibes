package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.d.DuelistOfDeepFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecrogenRotpriestTest extends BaseCardTest {

    @Test
    @DisplayName("A toxic creature dealing combat damage gives an additional poison counter")
    void toxicCreatureDealsAdditionalPoisonCounter() {
        addCreatureReady(player1, new NecrogenRotpriest());
        Permanent toxicCreature = addCreatureReady(player1, new DuelistOfDeepFaith());
        toxicCreature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The ability grants deathtouch to a target toxic creature you control")
    void grantsDeathtouchToTargetToxicCreature() {
        Permanent rotpriest = addCreatureReady(player1, new NecrogenRotpriest());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        addAbilityMana();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(rotpriest), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Granted deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent rotpriest = addCreatureReady(player1, new NecrogenRotpriest());
        Permanent target = addCreatureReady(player1, new CrawlingChorus());
        addAbilityMana();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(rotpriest), null,
                target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-toxic creature")
    void cannotTargetNonToxicCreature() {
        Permanent rotpriest = addCreatureReady(player1, new NecrogenRotpriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(rotpriest), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control with toxic");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
