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

    private void castPartyDude() {
        harness.setHand(player1, List.of(new PartyDude()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyArtifact(Permanent artifact) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, artifact.getId());
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
