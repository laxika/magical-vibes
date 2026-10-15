package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.w.WildwoodRebirth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyndicOfTithes.class, GreensideWatcher.class})
class SyndicOfTithesTest extends BaseCardTest {

    @Test
    @DisplayName("Paying Extort drains the opponent and gains life")
    void payingExtortDrainsOpponent() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Declining Extort does nothing")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Syndic of Tithes does not trigger for an opponent's spell")
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GreensideWatcher()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Extort goes on the stack before its payment decision")
    void paymentDecisionWaitsForResolution() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Extort can be paid with black mana")
    void payingExtortWithBlackDrainsOpponent() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Each Syndic offers a separate extort payment")
    void multipleSyndicsTriggerSeparately() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.addToBattlefield(player1, new SyndicOfTithes());
        harness.setHand(player1, List.of(new GreensideWatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Syndic of Tithes does not trigger its own extort")
    void castingSyndicDoesNotTriggerItsOwnExtort() {
        harness.setHand(player1, List.of(new SyndicOfTithes()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Syndic of Tithes");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @CardUsed({WildwoodRebirth.class})
    @DisplayName("Casting an instant also triggers extort")
    void instantSpellTriggersExtort() {
        harness.addToBattlefield(player1, new SyndicOfTithes());
        var creature = new GreensideWatcher();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WildwoodRebirth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Greenside Watcher");
    }
}
