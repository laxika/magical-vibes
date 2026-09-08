package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkutaBornOfAshTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers from the graveyard when the controller has more cards in hand")
    void triggersWithMoreCardsInHand() {
        AkutaBornOfAsh akuta = new AkutaBornOfAsh();
        harness.setGraveyard(player1, List.of(akuta));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Does not trigger when the controller does not have more cards than each opponent")
    void doesNotTriggerWithoutHandAdvantage() {
        harness.setGraveyard(player1, List.of(new AkutaBornOfAsh()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Rechecks the hand-size condition when the trigger resolves")
    void doesNotResolveAfterHandAdvantageIsLost() {
        harness.setGraveyard(player1, List.of(new AkutaBornOfAsh()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a Swamp returns Akuta from the graveyard to the battlefield")
    void sacrificeSwampReturnsAkuta() {
        AkutaBornOfAsh akuta = new AkutaBornOfAsh();
        harness.setGraveyard(player1, List.of(akuta));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(swamp.getId());

        harness.handlePermanentChosen(player1, swamp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(akuta.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(swamp.getId()))
                .anyMatch(permanent -> permanent.getId().equals(forest.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(akuta.getId()));
    }

    @Test
    @DisplayName("Declining the optional sacrifice leaves Akuta in the graveyard")
    void decliningSacrificeLeavesAkutaInGraveyard() {
        AkutaBornOfAsh akuta = new AkutaBornOfAsh();
        harness.setGraveyard(player1, List.of(akuta));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(akuta.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(swamp.getId()));
    }
}
