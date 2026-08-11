package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PridefulFeastlingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling lets Prideful Feastling benefit from Soldier tribal effects")
    void changelingCountsAsSoldier() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new PridefulFeastling());

        GameData gameData = harness.getGameData();
        Permanent feastling = findPermanent(player1, "Prideful Feastling");

        assertThat(gqs.getEffectivePower(gameData, feastling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gameData, feastling)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gameData, feastling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Prideful Feastling's lifelink gains its controller life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent feastling = new Permanent(new PridefulFeastling());
        feastling.setSummoningSick(false);
        feastling.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(feastling);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
