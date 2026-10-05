package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PickUpThePace.class, GrizzlyBears.class})
class PickUpThePaceTest extends BaseCardTest {

    @Test
    void exilesTopCardForCreatureThatEnteredThisTurn() {
        Permanent pace = harness.addToBattlefieldAndReturn(player1, new PickUpThePace());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(pace.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    void doesNotTriggerForCreatureThatEnteredEarlier() {
        harness.addToBattlefield(player1, new PickUpThePace());
        addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(1));

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void controllerMayCastTheExiledCardThisTurn() {
        harness.addToBattlefield(player1, new PickUpThePace());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void triggerStillExilesAfterEnchantmentLeaves() {
        Permanent pace = harness.addToBattlefieldAndReturn(player1, new PickUpThePace());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, pace));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastExiledCardAfterEnchantmentLeaves() {
        Permanent pace = harness.addToBattlefieldAndReturn(player1, new PickUpThePace());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, pace));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void canCastPreviouslyExiledCardOnLaterTurnOnlyAfterAttacking() {
        harness.addToBattlefield(player1, new PickUpThePace());
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        declareAttackers(player1, List.of(1));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }
}
