package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrainstealerDragon.class, GrizzlyBears.class})
class BrainstealerDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent's top card with persistent any-color play permission")
    void exilesEachOpponentsTopCardWithPersistentPermission() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));
        harness.addToBattlefield(player1, new BrainstealerDragon());

        resolveEndStepTrigger();

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    @Test
    @DisplayName("A nonland permanent cast from exile makes its owner lose its mana value")
    void castOpponentOwnedPermanentMakesOwnerLoseManaValueLife() {
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));
        harness.addToBattlefield(player1, new BrainstealerDragon());

        resolveEndStepTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();
    }
}
