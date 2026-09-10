package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.Battlegrowth;
import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGreatGoblin.class, Battlegrowth.class, GavonyTownship.class, GoblinAssailant.class,
        GrizzlyBears.class, Shock.class, Forest.class})
class TheGreatGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a counter on a Goblin deals 2 damage to a target opponent")
    void counterOnGoblinDamagesTargetOpponent() {
        harness.addToBattlefield(player1, new TheGreatGoblin());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());

        putCounterOn(goblin);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Putting a counter on a non-Goblin creature does not trigger the damage ability")
    void counterOnNonMatchingCreatureDoesNotDamageOpponent() {
        harness.addToBattlefield(player1, new TheGreatGoblin());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        putCounterOn(bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("When another Goblin dies, the top card is exiled with next-turn play permission")
    void goblinDeathExilesTopCardUntilEndOfNextTurn() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new TheGreatGoblin());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, goblin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void countersOnTwoGoblinsTriggerSeparatelyIncludingTheSource() {
        harness.addToBattlefield(player1, new TheGreatGoblin());
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GoblinAssailant());

        activateTownship(township);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void putCounterOn(Permanent creature) {
        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void activateTownship(Permanent township) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(township), 1, null, null);
        harness.passBothPriorities();
    }
}
