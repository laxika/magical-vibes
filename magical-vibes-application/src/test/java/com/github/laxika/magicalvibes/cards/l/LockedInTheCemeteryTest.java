package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LockedInTheCemetery.class, GrizzlyBears.class})
class LockedInTheCemeteryTest extends BaseCardTest {

    @Test
    @DisplayName("Five cards in the Aura controller's graveyard make the entering Aura tap the creature")
    void tapsEnchantedCreatureAtGraveyardThreshold() {
        Permanent creature = addCreatureReady(player2);
        harness.setGraveyard(player1, graveyardWithFiveCards());
        castAura(creature);

        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fewer than five cards in the Aura controller's graveyard do not tap the creature")
    void doesNotTapBelowGraveyardThreshold() {
        Permanent creature = addCreatureReady(player2);
        harness.setGraveyard(player1, graveyardWithFiveCards().subList(0, 4));
        castAura(creature);

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The opponent's graveyard does not satisfy the threshold")
    void opponentGraveyardDoesNotEnableTap() {
        Permanent creature = addCreatureReady(player2);
        harness.setGraveyard(player2, graveyardWithFiveCards());
        castAura(creature);

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The threshold is checked again when the enter trigger resolves")
    void thresholdCanFailBeforeTriggerResolves() {
        Permanent creature = addCreatureReady(player2);
        harness.setGraveyard(player1, graveyardWithFiveCards());
        castAura(creature);

        harness.passBothPriorities();
        harness.setGraveyard(player1, graveyardWithFiveCards().subList(0, 4));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enchanted creature does not untap while the Aura remains attached")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        attachAura(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature untaps after the Aura leaves the battlefield")
    void creatureUntapsAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        Permanent aura = attachAura(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addCreatureReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castAura(Permanent creature) {
        harness.setHand(player1, List.of(new LockedInTheCemetery()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new LockedInTheCemetery());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private List<Card> graveyardWithFiveCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
