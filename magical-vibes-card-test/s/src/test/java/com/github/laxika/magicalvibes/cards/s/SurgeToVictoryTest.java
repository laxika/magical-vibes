package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgeToVictory.class, DarkRitual.class, GrizzlyBears.class, Shock.class, Recollect.class})
class SurgeToVictoryTest extends BaseCardTest {

    @Test
    void exilesTheTargetAndBoostsOwnCreaturesByItsManaValue() {
        DarkRitual ritual = new DarkRitual();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(ritual));
        castSurge(ritual);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ritual);
    }

    @Test
    void combatDamageOffersAFreeCopyOfTheExiledCard() {
        Shock shock = new Shock();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void canExileASorceryAndCastItsCopyDuringCombat() {
        Recollect recollect = new Recollect();
        DarkRitual ritual = new DarkRitual();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(recollect, ritual));
        castSurge(recollect);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ritual.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ritual);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(recollect);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card instanceof Recollect);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void decliningTheCopyLeavesOnlyTheOriginalCardInExile() {
        Shock shock = new Shock();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void eachCreatureThatDealsCombatDamageOffersItsOwnCopy() {
        Shock shock = new Shock();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);

        declareAttackers(List.of(0, 1));
        resolveCombat();
        for (int i = 0; i < 2; i++) {
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void aCreatureEnteringLaterGetsNoBoostButStillTriggersTheCopy() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void opponentsCreaturesDoNotTriggerTheCopy() {
        Shock shock = new Shock();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
    }

    @Test
    void cannotTargetACreatureCard() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        assertThatThrownBy(() -> castSurge(bear)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);
    }

    @Test
    void cannotTargetAnOpponentsInstantCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        assertThatThrownBy(() -> castSurge(shock)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
    }

    @Test
    void aTargetLeavingTheGraveyardPreventsBothTheBoostAndDelayedAbility() {
        Shock shock = new Shock();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new SurgeToVictory()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void theBoostAndDelayedAbilityExpireAtEndOfTurn() {
        Shock shock = new Shock();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void recastingTheSameSurgeKeepsEachDelayedAbilityLinkedToItsOwnExiledCard() {
        SurgeToVictory surge = new SurgeToVictory();
        Shock shock = new Shock();
        Recollect recollect = new Recollect();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(surge, recollect));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveSorcery(player1, 0, List.of(shock.getId()));

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, surge.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(surge);

        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveSorcery(player1, 0, List.of(recollect.getId()));
        declareAttackers(List.of(0));
        resolveCombat();

        List<String> offeredCopies = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            PendingInteraction.MayAbilityChoice choice =
                    (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
            offeredCopies.add(choice.description());
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(offeredCopies).containsExactlyInAnyOrder(
                "Cast the copy of Shock without paying its mana cost?",
                "Cast the copy of Recollect without paying its mana cost?");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(shock, recollect);
    }

    private void castSurge(Card targetCard) {
        harness.setHand(player1, List.of(new SurgeToVictory()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveSorcery(player1, 0, List.of(targetCard.getId()));
    }
}
