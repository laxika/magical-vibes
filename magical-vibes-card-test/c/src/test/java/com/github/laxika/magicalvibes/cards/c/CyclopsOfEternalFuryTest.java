package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RottedHulk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyclopsOfEternalFury.class, RottedHulk.class})
class CyclopsOfEternalFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste")
    void grantsHasteToCreaturesYouControl() {
        Permanent cyclops = addCreatureReady(player1, new CyclopsOfEternalFury());
        Permanent creature = addCreatureReady(player1, new RottedHulk());
        Permanent opposingCreature = addCreatureReady(player2, new RottedHulk());

        assertThat(gqs.hasKeyword(gd, cyclops, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The Cyclops and creatures entering later can attack immediately")
    void newlyEnteredCreaturesCanAttack() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RottedHulk());
        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();

        Permanent cyclops = harness.enterBattlefieldAndReturn(player1, new CyclopsOfEternalFury());
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new RottedHulk());

        assertThat(als.canAttack(gd, cyclops, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, laterCreature, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Creatures lose granted haste as soon as the Cyclops leaves")
    void hasteEndsWhenCyclopsLeaves() {
        Permanent cyclops = harness.enterBattlefieldAndReturn(player1, new CyclopsOfEternalFury());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RottedHulk());
        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, cyclops));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Each Cyclops keeps granting haste while another leaves")
    void anotherCyclopsContinuesGrantingHaste() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new CyclopsOfEternalFury());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new CyclopsOfEternalFury());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new RottedHulk());
        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));

        assertThat(als.canAttack(gd, second, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();
    }
}
