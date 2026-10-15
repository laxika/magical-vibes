package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionHealer.class, GrizzlyBears.class})
class ExpeditionHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink only while you control another Cleric")
    void conditionalLifelink() {
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();

        addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();

        addCreatureReady(player2, new ExpeditionHealer());
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();

        addCreatureReady(player1, new ExpeditionHealer());
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Loses lifelink immediately when the other Cleric leaves")
    void losesLifelinkWhenOtherClericLeaves() {
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());
        Permanent otherCleric = addCreatureReady(player1, new ExpeditionHealer());

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCleric, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(otherCleric);

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Combat damage gains life while another Cleric is controlled")
    void combatDamageGainsLifeWithAnotherCleric() {
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());
        addCreatureReady(player1, new ExpeditionHealer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(healer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage does not gain life without another Cleric")
    void combatDamageDoesNotGainLifeAlone() {
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(healer.isTapped()).isFalse();
    }
}
