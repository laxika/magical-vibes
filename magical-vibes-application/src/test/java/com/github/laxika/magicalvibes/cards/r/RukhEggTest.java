package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.v.VolcanicHammer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateToken;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RukhEgg.class, VolcanicHammer.class, DarkBanishing.class})
class RukhEggTest extends BaseCardTest {

    @Test
    @DisplayName("Death registers a delayed trigger; no token appears immediately")
    void deathRegistersDelayedTrigger() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new VolcanicHammer()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0, harness.getPermanentId(player1, "Rukh Egg"));
        // Resolve Volcanic Hammer; egg dies.
        harness.passBothPriorities(); // resolve death trigger; register delayed token creation

        harness.assertInGraveyard(player1, "Rukh Egg");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedCreateToken.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        // No token yet; it only appears at the next end step.
        harness.assertNotOnBattlefield(player1, "Bird");
    }

    @Test
    @DisplayName("Creates a 4/4 red Bird with flying at the beginning of the next end step")
    void createsBirdTokenAtNextEndStep() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new VolcanicHammer()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0, harness.getPermanentId(player1, "Rukh Egg"));
        // Resolve Volcanic Hammer; egg dies.
        harness.passBothPriorities(); // resolve death trigger; register delayed token creation

        // Advance to the end step to fire the delayed trigger.
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities(); // resolve the token-creation trigger

        Permanent token = findPermanent(player1, "Bird");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }

    @Test
    @DisplayName("Death during an end step waits until the following player's end step")
    void deathDuringEndStepWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Rukh Egg"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rukh Egg");
        harness.assertNotOnBattlefield(player1, "Bird");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Bird");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bird");
        harness.assertNotOnBattlefield(player2, "Bird");
        assertThat(gd.getDelayedActions(DelayedCreateToken.class)).isEmpty();
    }

    @Test
    @DisplayName("A death creates exactly one Bird and does not repeat on later end steps")
    void delayedTriggerFiresOnlyOnce() {
        harness.addToBattlefield(player1, new RukhEgg());
        harness.setHand(player2, List.of(new VolcanicHammer()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0, harness.getPermanentId(player1, "Rukh Egg"));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bird")).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bird")).hasSize(1);
    }
}
