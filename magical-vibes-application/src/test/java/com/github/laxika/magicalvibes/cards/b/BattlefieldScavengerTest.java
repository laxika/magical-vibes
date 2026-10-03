package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloryBoundInitiate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlefieldScavenger.class, Forest.class, GloryBoundInitiate.class})
class BattlefieldScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addCreatureReady(player1, new BattlefieldScavenger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step and offers the loot")
    void exertSkipsUntapThenOffersLoot() {
        Permanent scavenger = addCreatureReady(player1, new BattlefieldScavenger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(scavenger.isTapped()).isTrue();
        assertThat(scavenger.getSkipUntapCount()).isGreaterThan(0);
        // The exert-matters ability fires as a second "may" prompt.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exerting then accepting the loot discards a card then draws a card")
    void exertThenLootDiscardsThenDraws() {
        addCreatureReady(player1, new BattlefieldScavenger());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GloryBoundInitiate()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // accept exert
        harness.handleMayAbilityChosen(player1, true); // accept loot

        // Discard happens before the draw.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0); // discard Glory-Bound Initiate

        harness.assertInGraveyard(player1, "Glory-Bound Initiate");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Exerting then declining the loot leaves the hand untouched")
    void exertThenDeclineLoot() {
        addCreatureReady(player1, new BattlefieldScavenger());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GloryBoundInitiate()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);  // accept exert
        harness.handleMayAbilityChosen(player1, false); // decline loot

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Glory-Bound Initiate");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining exert keeps the creature untapped-able and offers no loot")
    void decliningExertDoesNothing() {
        Permanent scavenger = addCreatureReady(player1, new BattlefieldScavenger());
        harness.setHand(player1, List.of(new GloryBoundInitiate()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(scavenger.getSkipUntapCount()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Exerting another creature triggers the Scavenger's rummage ability")
    void exertingAnotherCreatureOffersLoot() {
        harness.addToBattlefield(player1, new BattlefieldScavenger());
        addCreatureReady(player1, new GloryBoundInitiate());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GloryBoundInitiate()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Glory-Bound Initiate");
    }

    @Test
    @DisplayName("Each Scavenger triggers when one Scavenger exerts")
    void eachScavengerOffersLootForOneExert() {
        addCreatureReady(player1, new BattlefieldScavenger());
        harness.addToBattlefield(player1, new BattlefieldScavenger());
        harness.setHand(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting rummage with an empty hand does not draw")
    void emptyHandCannotDrawWithoutDiscarding() {
        addCreatureReady(player1, new BattlefieldScavenger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
