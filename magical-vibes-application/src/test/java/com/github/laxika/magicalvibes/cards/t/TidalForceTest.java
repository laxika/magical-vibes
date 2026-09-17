package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidalForce.class, GrizzlyBears.class})
class TidalForceTest extends BaseCardTest {

    @Test
    void controllerMayTapTargetPermanentDuringAnyUpkeep() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void controllerMayUntapTargetPermanentDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void decliningMayAbilityLeavesTargetUnchanged() {
        harness.addToBattlefield(player1, new TidalForce());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void shroudedPermanentIsNotAValidTarget() {
        harness.addToBattlefield(player1, new TidalForce());
        Card shroudedCard = new GrizzlyBears();
        shroudedCard.setKeywords(Set.of(Keyword.SHROUD));
        Permanent shroudedTarget = harness.addToBattlefieldAndReturn(player1, shroudedCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(shroudedTarget.getId());
    }
}
