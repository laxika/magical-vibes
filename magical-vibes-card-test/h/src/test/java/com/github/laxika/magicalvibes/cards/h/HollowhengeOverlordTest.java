package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessWaif;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowhengeOverlord.class, GrizzlyBears.class, RecklessWaif.class, SnarlingWolf.class})
class HollowhengeOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 2/2 Wolf for each Wolf or Werewolf you control at your upkeep")
    void createsWolfTokensForControlledWolvesAndWerewolves() {
        harness.addToBattlefield(player1, new HollowhengeOverlord());
        harness.addToBattlefield(player1, new SnarlingWolf());
        harness.addToBattlefield(player1, new RecklessWaif());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(2);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new HollowhengeOverlord());
        harness.addToBattlefield(player1, new SnarlingWolf());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }
}
