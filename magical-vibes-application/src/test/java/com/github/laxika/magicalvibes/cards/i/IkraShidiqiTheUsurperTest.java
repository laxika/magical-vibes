package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IkraShidiqiTheUsurper.class, GrizzlyBears.class})
class IkraShidiqiTheUsurperTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to the toughness of each creature that deals combat damage")
    void gainsLifeForEachDamagingCreature() {
        Permanent ikra = addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        ikra.setAttacking(true);
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(29);
    }

    @Test
    @DisplayName("Ikra does not trigger for an opponent's creature")
    void ignoresOpponentsCreature() {
        addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
