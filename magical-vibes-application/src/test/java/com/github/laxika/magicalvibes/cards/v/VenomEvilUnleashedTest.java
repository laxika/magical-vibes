package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KravensCats;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomEvilUnleashed.class, KravensCats.class, Mountain.class})
class VenomEvilUnleashedTest extends BaseCardTest {

    private void readyAbility() {
        harness.setGraveyard(player1, List.of(new VenomEvilUnleashed()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Ability puts two +1/+1 counters on target creature and grants deathtouch")
    void abilityBoostsTargetCreature() {
        Permanent cats = addCreatureReady(player2, new KravensCats());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, cats.getId());
        harness.passBothPriorities();

        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cats, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Exiling the card is part of the activation cost")
    void exilesSourceAsCost() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, cats.getId());

        harness.assertNotInGraveyard(player1, "Venom, Evil Unleashed");
    }

    @Test
    @DisplayName("Deathtouch granted by the ability expires at cleanup")
    void deathtouchExpiresAtEndOfTurn() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        readyAbility();

        harness.activateGraveyardAbility(player1, 0, cats.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, cats, Keyword.DEATHTOUCH)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, cats, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Ability requires a creature target")
    void abilityRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyAbility();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can only be activated as a sorcery")
    void abilityIsSorcerySpeedOnly() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        harness.setGraveyard(player1, List.of(new VenomEvilUnleashed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, cats.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated outside a main phase")
    void cannotActivateDuringCombat() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        readyAbility();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, cats.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Venom, Evil Unleashed");
        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ability cannot be activated while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        readyAbility();
        harness.setGraveyard(player1, List.of(new VenomEvilUnleashed(), new VenomEvilUnleashed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0, cats.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, cats.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Venom, Evil Unleashed");

        harness.passBothPriorities();
        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated in the postcombat main phase")
    void canActivateDuringPostcombatMainPhase() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        readyAbility();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, cats.getId());
        harness.passBothPriorities();

        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cats, Keyword.DEATHTOUCH)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cats, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Insufficient mana prevents activation without exiling the source")
    void cannotActivateWithoutBlackMana() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        harness.setGraveyard(player1, List.of(new VenomEvilUnleashed()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, cats.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Venom, Evil Unleashed");
        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A target leaving the battlefield does not refund the exile cost")
    void removedTargetDoesNotReceiveEffects() {
        Permanent cats = addCreatureReady(player1, new KravensCats());
        readyAbility();
        harness.activateGraveyardAbility(player1, 0, cats.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Venom, Evil Unleashed"));
        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, cats));

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Venom, Evil Unleashed");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Venom, Evil Unleashed"));
        harness.assertInHand(player1, "Kraven's Cats");
        assertThat(cats.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
