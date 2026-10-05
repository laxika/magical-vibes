package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({PartyDude.class, Memnite.class, Shock.class})
class PartyDudeTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void clearInitialHand() {
        harness.setHand(player1, List.of());
    }

    @Test
    @DisplayName("When it enters, each player creates a Food token")
    void eachPlayerCreatesFoodOnEntry() {
        castPartyDude();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 2, draws when an artifact an opponent controls goes to a graveyard")
    void levelTwoDrawsForOpponentArtifact() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        Permanent ownArtifact = addCreatureReady(player1, new Memnite());
        Permanent opponentArtifact = addCreatureReady(player2, new Memnite());

        destroyArtifact(ownArtifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        destroyArtifact(opponentArtifact);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("At level 3, boosts up to one attacker by the controller's hand size")
    void levelThreeBoostsAttackerByHandSizeAtResolution() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        levelUp(partyDude, 1, 4);
        Permanent attacker = addCreatureReady(player1, new Memnite());
        harness.setHand(player1, List.of(new Memnite(), new Memnite()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(attacker.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.setHand(player1, List.of(new Memnite(), new Memnite(), new Memnite(), new Memnite()));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Advancing a Class changes its level without placing counters")
    void advancingClassDoesNotPlaceCounters() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());

        levelUp(partyDude, 0, 1);

        assertThat(partyDude.getClassLevel()).isEqualTo(2);
        assertThat(partyDude.getCounters()).isEmpty();

        levelUp(partyDude, 1, 4);

        assertThat(partyDude.getClassLevel()).isEqualTo(3);
        assertThat(partyDude.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact dying at level 1 does not trigger the Class")
    void levelOneDoesNotTriggerForOpponentArtifact() {
        harness.addToBattlefield(player1, new PartyDude());
        Permanent artifact = addCreatureReady(player2, new Memnite());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof PartyDude);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking at level 1 does not ask for a boost target")
    void attackAbilityIsAbsentBeforeLevelThree() {
        harness.addToBattlefield(player1, new PartyDude());
        Permanent attacker = addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof PartyDude);
    }

    @Test
    @DisplayName("At level 2, attacking does not ask for a boost target")
    void levelTwoDoesNotTriggerAttackAbility() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        Permanent attacker = addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof PartyDude);
    }

    @Test
    @DisplayName("Each player's Food can be sacrificed to gain life, and opposing Food draws at level 2")
    void foodTokensHaveTheirNormalAbility() {
        castPartyDude();
        Permanent partyDude = findPermanent(player1, "Party Dude");
        levelUp(partyDude, 0, 1);
        harness.setLibrary(player1, List.of(new PartyDude(), new PartyDude()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food")),
                0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(findPermanent(player2, "Food")),
                0, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        assertThat(countPermanents(player2, "Food")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Level 3 retains the artifact-death draw ability")
    void levelThreeRetainsDrawAbility() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        levelUp(partyDude, 1, 4);
        harness.setLibrary(player1, List.of(new PartyDude()));

        destroyArtifact(addCreatureReady(player2, new Memnite()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The artifact's controller, rather than its graveyard owner, determines the draw")
    void opponentControlledArtifactDrawsEvenWhenWeOwnIt() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        harness.setLibrary(player1, List.of(new PartyDude()));
        Memnite stolenArtifact = new Memnite();
        stolenArtifact.setOwnerId(player1.getId());
        Permanent artifact = addCreatureReady(player2, stolenArtifact);

        destroyArtifact(artifact);

        harness.assertInGraveyard(player1, "Memnite");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Two attackers cause one trigger, and choosing no target is allowed")
    void multipleAttackersTriggerOnceAndTargetCanBeDeclined() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        levelUp(partyDude, 1, 4);
        Permanent first = addCreatureReady(player1, new Memnite());
        Permanent second = addCreatureReady(player1, new Memnite());
        harness.setHand(player1, List.of(new PartyDude(), new PartyDude()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost is fixed on resolution and expires at the end of the turn")
    void boostDoesNotTrackLaterHandChangesAndExpires() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        levelUp(partyDude, 1, 4);
        Permanent attacker = addCreatureReady(player1, new Memnite());
        harness.setHand(player1, List.of(new PartyDude(), new PartyDude()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("With an empty hand, a chosen attacker gets no bonus")
    void emptyHandGivesZeroBonus() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        levelUp(partyDude, 0, 1);
        levelUp(partyDude, 1, 4);
        Permanent attacker = addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Class cannot skip level 2, repeat level 2, or gain a level during combat")
    void classLevelActivationRestrictions() {
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(partyDude);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        levelUp(partyDude, 0, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPartyDude() {
        harness.setHand(player1, List.of(new PartyDude()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }

    private void destroyArtifact(Permanent artifact) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        resolveAllTriggers();
    }

    private void levelUp(Permanent partyDude, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int partyDudeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(partyDude);
        harness.activateAbility(player1, partyDudeIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }
}
