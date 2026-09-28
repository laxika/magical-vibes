package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathleaperTerrorWeapon.class, GrizzlyBears.class})
class DeathleaperTerrorWeaponTest extends BaseCardTest {

    @Test
    void grantsDoubleStrikeToControlledCreaturesThatEnteredThisTurn() {
        Permanent deathleaper = harness.addToBattlefieldAndReturn(player1, new DeathleaperTerrorWeapon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        markEnteredThisTurn(player1.getId(), deathleaper);
        markEnteredThisTurn(player1.getId(), creature);

        assertThat(gqs.hasKeyword(gd, deathleaper, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doesNotGrantDoubleStrikeToCreaturesThatDidNotEnterOrThatAnOpponentControls() {
        harness.addToBattlefield(player1, new DeathleaperTerrorWeapon());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        markEnteredThisTurn(player2.getId(), opponentCreature);

        assertThat(gqs.hasKeyword(gd, oldCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void markEnteredThisTurn(java.util.UUID controllerId, Permanent permanent) {
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                .add(permanent.getCard());
    }
}
