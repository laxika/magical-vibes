package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EldraziMonument;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfestedThrinax.class, GrizzlyBears.class, EldraziMonument.class})
class InfestedThrinaxTest extends BaseCardTest {

    @Test
    void createsSaprolingsEqualToDyingCreaturePower() {
        castInfestedThrinax();
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        dyingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    void doesNotTriggerForTokenOrOpponentCreature() {
        castInfestedThrinax();
        Permanent token = harness.addToBattlefieldAndReturn(player1, saprolingToken());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        token.setMarkedDamage(1);
        opponentCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void triggerExpiresAtEndOfTurn() {
        castInfestedThrinax();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dyingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    private void castInfestedThrinax() {
        castInfestedThrinaxWithoutResolving();
        resolveAllTriggers();
    }

    @Test
    void triggersForItsOwnDeathAndContinuesAfterLeavingBattlefield() {
        castInfestedThrinax();
        Permanent thrinax = findPermanent(player1, "Infested Thrinax");
        thrinax.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(4);
        harness.assertInGraveyard(player1, "Infested Thrinax");

        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new InfestedThrinax());
        dyingCreature.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(8);
    }

    @Test
    void usesPowerIncludingCountersImmediatelyBeforeDeath() {
        castInfestedThrinax();
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new InfestedThrinax());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        dyingCreature.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(7);
    }

    @Test
    void usesLastKnownPowerIncludingStaticBonuses() {
        castInfestedThrinax();
        harness.addToBattlefield(player1, new EldraziMonument());
        Permanent dyingCreature = findPermanent(player1, "Infested Thrinax");
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, dyingCreature);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Infested Thrinax");
        assertThat(findPermanents(player1, "Saproling")).hasSize(5);
    }

    @Test
    void eachResolvedEntersAbilityRegistersAnIndependentTrigger() {
        castInfestedThrinax();
        castInfestedThrinax();
        Permanent dyingCreature = findPermanent(player1, "Infested Thrinax");
        dyingCreature.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(8);
    }

    @Test
    void deathBeforeEntersAbilityResolvesDoesNotCreateTokens() {
        castInfestedThrinaxWithoutResolving();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Infested Thrinax");
        assertThat(gd.stack).hasSize(1);

        Permanent dyingCreature = findPermanent(player1, "Infested Thrinax");
        dyingCreature.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new InfestedThrinax());
        laterCreature.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(4);
    }

    private void castInfestedThrinaxWithoutResolving() {
        harness.setHand(player1, List.of(new InfestedThrinax()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private Card saprolingToken() {
        Card card = new Card();
        card.setName("Saproling");
        card.setType(CardType.CREATURE);
        card.setToken(true);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        return card;
    }
}
