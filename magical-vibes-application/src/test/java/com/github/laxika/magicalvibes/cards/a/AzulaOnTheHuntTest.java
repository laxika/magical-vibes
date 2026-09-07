package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AzulaOnTheHunt.class)
class AzulaOnTheHuntTest extends BaseCardTest {

    @Test
    void attackingAddsManaLosesLifeAndCreatesClue() {
        addAzulaReady();
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void firebendingManaLastsThroughCombatButNotBeyondIt() {
        addAzulaReady();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addAzulaReady() {
        Permanent azula = new Permanent(new AzulaOnTheHunt());
        azula.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(azula);
        return azula;
    }
}
