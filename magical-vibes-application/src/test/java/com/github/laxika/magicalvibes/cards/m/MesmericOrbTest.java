package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Domineer;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesmericOrb.class, Mountain.class, MyrEnforcer.class, Domineer.class, Regress.class})
class MesmericOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Each untapped permanent makes its controller mill a card")
    void eachUntappedPermanentMillsItsController() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player1Permanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        player1Permanent.tap();
        trimDeck(player1, 10);

        advanceToUpkeep(player1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's untapped permanent makes that opponent mill a card")
    void opponentsUntappedPermanentMillsOpponent() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new Mountain());
        player2Permanent.tap();
        trimDeck(player2, 10);

        advanceToUpkeep(player2);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each permanent that becomes untapped creates a separate mill trigger")
    void eachBecomesUntappedEventCreatesSeparateMillTrigger() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent firstPermanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondPermanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        firstPermanent.tap();
        secondPermanent.tap();
        trimDeck(player1, 10);

        advanceToUpkeep(player1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An already untapped permanent does not trigger Mesmeric Orb")
    void alreadyUntappedPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesmericOrb());
        harness.addToBattlefield(player1, new Mountain());
        trimDeck(player1, 10);

        advanceToUpkeep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller is remembered if the untapped permanent leaves before resolution")
    void remembersControllerIfPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new Mountain());
        player2Permanent.tap();
        trimDeck(player2, 10);

        harness.inMutationScope(() -> {
            player2Permanent.untap();
            harness.getTriggerCollectionService().checkBecomesUntappedTriggers(gd, player2Permanent);
        });
        gd.playerBattlefields.get(player2.getId()).remove(player2Permanent);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Untap-step triggers wait until upkeep and resolve separately")
    void untapStepTriggersWaitUntilUpkeep() {
        harness.addToBattlefield(player1, new MesmericOrb());
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        advanceToUpkeep(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mesmeric Orb triggers when it becomes untapped itself")
    void orbUntappingTriggersItself() {
        harness.addToBattlefieldAndReturn(player1, new MesmericOrb()).tap();
        harness.setLibrary(player1, List.of(new Mountain()));

        advanceToUpkeep(player1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Orbs controlled by different players each trigger on the same untap")
    void multipleOrbsEachMillTheUntappedPermanentsController() {
        harness.addToBattlefield(player1, new MesmericOrb());
        harness.addToBattlefield(player2, new MesmericOrb());
        harness.addToBattlefieldAndReturn(player2, new Mountain()).tap();
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        advanceToUpkeep(player2);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milling an empty library does not make a player lose")
    void millingEmptyLibraryDoesNotCauseLoss() {
        harness.addToBattlefield(player1, new MesmericOrb());
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A queued Orb ability still mills after the Orb returns to hand")
    void abilityResolvesAfterOrbLeavesBattlefield() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new MesmericOrb());
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new Regress()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, orb.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        harness.assertInHand(player1, "Mesmeric Orb");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("The permanent's controller at resolution mills after its control changes")
    void usesCurrentControllerAfterControlChangesBeforeResolution() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MyrEnforcer());
        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        harness.assertOnBattlefield(player1, "Myr Enforcer");
        creature.tap();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.setHand(player1, List.of(new Regress()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Domineer"));
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);
        harness.assertOnBattlefield(player2, "Myr Enforcer");
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("A face-down Mesmeric Orb has no printed untap-triggered ability")
    void faceDownOrbDoesNotTrigger() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new MesmericOrb());
        orb.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setLibrary(player1, List.of(new Mountain()));

        advanceToUpkeep(player1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Orb that has lost its abilities does not trigger")
    void orbWithAbilitiesRemovedDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new MesmericOrb())
                .setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setLibrary(player1, List.of(new Mountain()));

        advanceToUpkeep(player1);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void trimDeck(Player player, int size) {
        var library = gd.playerDecks.get(player.getId());
        harness.setLibrary(player, library.subList(Math.max(0, library.size() - size), library.size()));
    }

}
