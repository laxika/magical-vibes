package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WishingWell.class, Shock.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class WishingWellTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a coin counter on itself even without a matching graveyard card")
    void putsCounterWithoutMatchingGraveyardCard() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WishingWell());
        harness.setGraveyard(player1, List.of(new CounselOfTheSoratami()));

        activateAndResolveAbility();

        assertThat(well.getCounterCount(CounterType.COIN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Only targets an instant or sorcery whose mana value matches the new coin count")
    void targetsExactManaValue() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(shock, counsel));

        activateAndResolveAbility();
        resolveReflexiveTrigger();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Shock").doesNotContain("Counsel");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock, counsel);
    }

    @Test
    @DisplayName("Casts the targeted matching instant for free and exiles it after resolution")
    void castsMatchingInstantAndExilesIt() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateAndResolveAbility();
        resolveReflexiveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Casts a matching sorcery for free using the accumulated coin count")
    void castsMatchingSorcery() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WishingWell());
        well.setCounterCount(CounterType.COIN, 2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock firstDraw = new Shock();
        Shock secondDraw = new Shock();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Shock()));
        harness.setGraveyard(player1, List.of(counsel));

        activateAndResolveAbility();
        resolveReflexiveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(well.getCounterCount(CounterType.COIN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(counsel);
    }

    @Test
    @DisplayName("A creature with matching mana value is not a legal graveyard target")
    void doesNotTargetMatchingCreature() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WishingWell());
        well.setCounterCount(CounterType.COIN, 1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        activateAndResolveAbility();

        assertThat(well.getCounterCount(CounterType.COIN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Chooses one target when multiple graveyard cards match")
    void choosesAmongMatchingCards() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));

        activateAndResolveAbility();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);
        resolveReflexiveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
    }

    @Test
    @DisplayName("The target becomes illegal if the coin count changes before the reflexive trigger resolves")
    void rechecksCoinCountOnResolution() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        activateAndResolveAbility();
        well.setCounterCount(CounterType.COIN, 2);
        resolveReflexiveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Does not activate during combat")
    void cannotActivateAtInstantSpeed() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast a target that has left the graveyard")
    void targetMustRemainInGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        activateAndResolveAbility();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(shock));
        resolveReflexiveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Uses the last known coin count if Wishing Well leaves before its trigger resolves")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        activateAndResolveAbility();
        gd.playerBattlefields.get(player1.getId()).remove(well);
        harness.setHand(player1, List.of(well.getCard()));
        resolveReflexiveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    private void activateAndResolveAbility() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void resolveReflexiveTrigger() {
        harness.passBothPriorities();
    }
}
