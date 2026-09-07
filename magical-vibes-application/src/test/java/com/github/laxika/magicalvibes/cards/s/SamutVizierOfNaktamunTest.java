package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamutVizierOfNaktamun.class, GrizzlyBears.class})
class SamutVizierOfNaktamunTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that entered this turn dealing combat damage draws a card")
    void creatureEnteredThisTurnDrawsCard() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        var attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A creature that did not enter this turn does not trigger")
    void creatureDidNotEnterThisTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new SamutVizierOfNaktamun());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        var attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
