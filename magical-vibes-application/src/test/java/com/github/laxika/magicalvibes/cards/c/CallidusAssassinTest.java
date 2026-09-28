package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallidusAssassin.class, GrizzlyBears.class, AirElemental.class})
class CallidusAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Callidus Assassin copies a creature and destroys another creature with the same name")
    void copiesAndDestroysAnotherCreatureWithTheSameName() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCallidus();
        chooseCopy(firstBear.getId());
        harness.handlePermanentChosen(player1, secondBear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(firstBear.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getName().equals("Callidus Assassin")
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Callidus Assassin cannot target a creature with a different name")
    void cannotTargetDifferentNameCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castCallidus();
        chooseCopy(bear.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, airElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Callidus Assassin may decline its optional destruction")
    void mayDeclineDestruction() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCallidus();
        chooseCopy(firstBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(firstBear.getId(), secondBear.getId());
    }

    @Test
    @DisplayName("Declining to copy leaves Callidus Assassin without the copied trigger")
    void decliningToCopyLeavesOriginalCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCallidus();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getName().equals("Callidus Assassin")
                        && permanent.getCard().getName().equals("Callidus Assassin"));
        assertThat(gd.stack).isEmpty();
    }

    private void castCallidus() {
        harness.castFromHand(player1, new CallidusAssassin(), "{4}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseCopy(UUID targetId) {
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
    }
}
