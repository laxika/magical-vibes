package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AmrouScout;
import com.github.laxika.magicalvibes.cards.a.AmrouSeekers;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.h.HerdGnarr;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingEnd.class, AmrouScout.class, AmrouSeekers.class, AshcoatBear.class,
        BenalishCavalry.class, HerdGnarr.class, Island.class})
class LivingEndTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Living End with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        LivingEnd card = new LivingEnd();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("Living End replaces creatures with graveyard creatures and leaves noncreatures")
    void replacesCreaturesFromGraveyards() {
        suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new BenalishCavalry());
        harness.setGraveyard(player1, List.of(new AmrouScout(), new Island()));
        harness.setGraveyard(player2, List.of(new AmrouSeekers(), new Island()));

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Amrou Scout");
        harness.assertOnBattlefield(player2, "Amrou Seekers");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Living End cannot be cast normally from hand")
    void cannotCastNormally() {
        LivingEnd card = new LivingEnd();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 10);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend uses no stack and removes counters only during the owner's upkeep")
    void countersWaitForOwnersUpkeep() {
        LivingEnd card = suspendCard();
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Living End exiled without counters")
    void canDeclineSuspendCast() {
        LivingEnd card = suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new AmrouScout()));

        advanceToSuspendCastChoice();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Amrou Scout");

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Empty graveyards still result in creatures being sacrificed, while lands remain")
    void emptyGraveyardsStillSacrificeCreatures() {
        suspendCard();
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new BenalishCavalry());
        harness.addToBattlefield(player1, new Island());

        resolveSuspendedCard();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Benalish Cavalry");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Living End");
    }

    @Test
    @DisplayName("Only creatures exiled by Living End return, even with empty battlefields")
    void doesNotReturnPreviouslyExiledCreatures() {
        AshcoatBear previouslyExiled = new AshcoatBear();
        harness.setExile(player1, List.of(previouslyExiled));
        suspendCard();
        harness.setGraveyard(player1, List.of(new AmrouScout(), new Island()));
        harness.setGraveyard(player2, List.of(new AmrouSeekers()));

        resolveSuspendedCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(previouslyExiled);
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player1, "Amrou Scout");
        harness.assertOnBattlefield(player2, "Amrou Seekers");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Returning creatures see other creatures entering simultaneously")
    void returningCreaturesSeeSimultaneousEntries() {
        suspendCard();
        harness.setGraveyard(player1,
                List.of(new HerdGnarr(), new AmrouScout(), new AshcoatBear()));
        harness.setGraveyard(player2, List.of(new BenalishCavalry()));

        resolveSuspendedCard();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Herd Gnarr"))).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Herd Gnarr"))).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Benalish Cavalry");
    }

    @Test
    @DisplayName("Living End cannot be suspended during upkeep")
    void cannotSuspendOutsideSorceryTiming() {
        LivingEnd card = new LivingEnd();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void advanceToSuspendCastChoice() {
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
    }

    private void resolveSuspendedCard() {
        advanceToSuspendCastChoice();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
    }

    private LivingEnd suspendCard() {
        LivingEnd card = new LivingEnd();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
