package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderwineProphets.class, DeeptreadMerrow.class, GoldmeadowHarrier.class})
class WanderwineProphetsTest extends BaseCardTest {

    private void castWanderwineProphets() {
        harness.castFromHand(player1, new WanderwineProphets(), "{4}{U}{U}");
        harness.passBothPriorities(); // resolve the creature spell -> champion ETB on the stack
    }

    @Test
    @DisplayName("Sacrifices itself when there is no other Merfolk to champion")
    void sacrificesItselfWithoutAnotherMerfolk() {
        castWanderwineProphets();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wanderwine Prophets");
        harness.assertInGraveyard(player1, "Wanderwine Prophets");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Championing another Merfolk exiles it until Wanderwine Prophets leaves")
    void championsAnotherMerfolkUntilItLeaves() {
        Permanent merrow = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());

        castWanderwineProphets();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(merrow.getId());

        harness.handlePermanentChosen(player1, merrow.getId());

        harness.assertOnBattlefield(player1, "Wanderwine Prophets");
        harness.assertNotOnBattlefield(player1, "Deeptread Merrow");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Deeptread Merrow"));

        Permanent prophets = findPermanent(player1, "Wanderwine Prophets");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, prophets));

        harness.assertNotOnBattlefield(player1, "Deeptread Merrow");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deeptread Merrow");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Deeptread Merrow"));
    }

    @Test
    @DisplayName("Champion offers only another Merfolk controlled by its controller")
    void championChoiceOnlyOffersAnotherControlledMerfolk() {
        Permanent merrow = harness.addToBattlefieldAndReturn(player1, new DeeptreadMerrow());
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());

        castWanderwineProphets();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(merrow.getId())
                .doesNotContain(harrier.getId());
    }

    @Test
    @DisplayName("Combat damage trigger presents the may ability choice")
    void combatDamagePresentsMayChoice() {
        Permanent prophets = addCreatureReady(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting and sacrificing a Merfolk grants an extra turn")
    void sacrificeMerfolkGrantsExtraTurn() {
        Permanent prophets = addCreatureReady(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        // Accept the may -> prompted to choose which Merfolk to sacrifice (only Prophets qualifies).
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, prophets.getId());

        harness.assertInGraveyard(player1, "Wanderwine Prophets");
        assertThat(gd.extraTurns).contains(player1.getId());
    }

    @Test
    @DisplayName("Sacrificing another Merfolk grants an extra turn and leaves Wanderwine Prophets")
    void sacrificingAnotherMerfolkGrantsExtraTurn() {
        Permanent prophets = addCreatureReady(player1, new WanderwineProphets());
        Permanent otherProphets = addCreatureReady(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherProphets.getId());

        harness.assertOnBattlefield(player1, "Wanderwine Prophets");
        harness.assertInGraveyard(player1, "Wanderwine Prophets");
        assertThat(gd.extraTurns).contains(player1.getId());
    }

    @Test
    @DisplayName("Declining the may ability grants no extra turn and sacrifices nothing")
    void decliningGrantsNoExtraTurn() {
        Permanent prophets = addCreatureReady(player1, new WanderwineProphets());
        prophets.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.extraTurns).doesNotContain(player1.getId());
        harness.assertOnBattlefield(player1, "Wanderwine Prophets");
    }

    @Test
    @DisplayName("Champion excludes Merfolk controlled by the opponent")
    void cannotChampionOpponentsMerfolk() {
        harness.addToBattlefield(player2, new DeeptreadMerrow());

        castWanderwineProphets();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wanderwine Prophets");
        harness.assertOnBattlefield(player2, "Deeptread Merrow");
    }

    @Test
    @DisplayName("Champion can be declined even with an eligible Merfolk")
    void mayDeclineChampionWithEligibleMerfolk() {
        harness.addToBattlefield(player1, new DeeptreadMerrow());

        castWanderwineProphets();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        } else {
            harness.handlePermanentChosen(player1, null);
        }

        harness.assertInGraveyard(player1, "Wanderwine Prophets");
        harness.assertOnBattlefield(player1, "Deeptread Merrow");
        assertThat(gd.extraTurns).isEmpty();
    }
}
