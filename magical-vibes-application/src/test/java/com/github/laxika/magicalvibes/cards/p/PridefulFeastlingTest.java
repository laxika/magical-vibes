package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PridefulFeastling.class, FieldMarshal.class})
class PridefulFeastlingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling lets Prideful Feastling benefit from Soldier tribal effects")
    void changelingCountsAsSoldier() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new PridefulFeastling());

        Permanent feastling = findPermanent(player1, "Prideful Feastling");

        assertThat(gqs.getEffectivePower(gd, feastling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, feastling)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, feastling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Prideful Feastling's lifelink gains its controller life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent feastling = addCreatureReady(player1, new PridefulFeastling());
        feastling.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Both attacking and blocking Feastlings gain life from damage to creatures")
    void lifelinkGainsLifeForBothControllersInBlockedCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PridefulFeastling());
        harness.addToBattlefield(player2, new PridefulFeastling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertOnBattlefield(player1, "Prideful Feastling");
        harness.assertOnBattlefield(player2, "Prideful Feastling");
    }
}
