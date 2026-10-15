package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.FaerieTauntings;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistbindClique.class, AvianChangeling.class, Island.class, GoldmeadowHarrier.class, FaerieTauntings.class})
class MistbindCliqueTest extends BaseCardTest {

    /** Casts Mistbind Clique, resolves its champion ETB, and champions the given Faerie —
     *  stopping once the "championed" trigger is awaiting a target player. */
    private void championFaerie(UUID faerieId) {
        harness.castFromHand(player1, new MistbindClique(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> champion permanent choice
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.handlePermanentChosen(player1, faerieId); // champion the Faerie -> championed trigger
    }

    @Test
    @DisplayName("Championing a Faerie prompts a target-player choice")
    void championingPromptsTargetPlayerChoice() {
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        // Championed creature is exiled; Mistbind Clique stays.
        harness.assertOnBattlefield(player1, "Mistbind Clique");
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
    }

    @Test
    @DisplayName("Taps all lands the chosen opponent controls")
    void tapsAllLandsTargetPlayerControls() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);
        harness.handlePermanentChosen(player1, player2.getId()); // target the opponent
        harness.passBothPriorities(); // resolve the championed trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Target player can be its controller (taps own lands)")
    void canTargetOwnLands() {
        harness.addToBattlefield(player1, new Island());
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);
        harness.handlePermanentChosen(player1, player1.getId()); // target self
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Only lands are tapped, not other permanents")
    void tapsOnlyLands() {
        harness.addToBattlefield(player2, new Island());
        Permanent harrier = harness.addToBattlefieldAndReturn(player2, new GoldmeadowHarrier());
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(harrier.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Returns the championed Faerie when Mistbind Clique leaves")
    void returnsChampionWhenCliqueLeaves() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());

        championFaerie(faerie.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent clique = findPermanent(player1, "Mistbind Clique");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, clique));

        harness.assertNotOnBattlefield(player1, "Mistbind Clique");
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avian Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Avian Changeling"));
    }

    @Test
    @DisplayName("Does not champion a non-Faerie creature")
    void doesNotChampionNonFaerieCreature() {
        harness.addToBattlefield(player1, new GoldmeadowHarrier());

        harness.castFromHand(player1, new MistbindClique(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mistbind Clique");
        harness.assertInGraveyard(player1, "Mistbind Clique");
        harness.assertOnBattlefield(player1, "Goldmeadow Harrier");
    }

    @Test
    @DisplayName("No championed trigger when there is no Faerie to champion")
    void noTriggerWhenNoFaerie() {
        harness.addToBattlefield(player2, new Island());

        harness.castFromHand(player1, new MistbindClique(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> no Faerie -> sacrifice, no trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mistbind Clique");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Can sacrifice Clique instead of championing an available Faerie")
    void canDeclineChampionWithAvailableFaerie() {
        harness.addToBattlefield(player1, new AvianChangeling());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castFromHand(player1, new MistbindClique(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Mistbind Clique");
        harness.assertNotOnBattlefield(player1, "Mistbind Clique");
        harness.assertOnBattlefield(player1, "Avian Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(island.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Champion offers only another Faerie controlled by Clique's controller")
    void championExcludesItselfOpponentsAndNonFaeries() {
        Permanent ownFaerie = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        harness.addToBattlefield(player2, new AvianChangeling());
        harness.addToBattlefield(player1, new GoldmeadowHarrier());

        harness.castFromHand(player1, new MistbindClique(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ownFaerie.getId());
    }

    @Test
    @DisplayName("The tap trigger still resolves after Clique leaves the battlefield")
    void tapTriggerSurvivesCliqueLeaving() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());

        championFaerie(faerie.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent clique = findPermanent(player1, "Mistbind Clique");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, clique));

        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        assertThat(island.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avian Changeling");
        assertThat(island.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps lands present at resolution and leaves the other player's lands alone")
    void tapsCurrentLandsOfOnlyTargetPlayer() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opposingIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());

        championFaerie(faerie.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent newIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        assertThat(opposingIsland.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(opposingIsland.isTapped()).isTrue();
        assertThat(newIsland.isTapped()).isTrue();
        assertThat(ownIsland.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can champion a noncreature Faerie permanent")
    void canChampionFaerieEnchantment() {
        Permanent tauntings = harness.addToBattlefieldAndReturn(player1, new FaerieTauntings());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        championFaerie(tauntings.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Faerie Tauntings");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Faerie Tauntings"));
        assertThat(island.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Mistbind Clique");
    }

    @Test
    @DisplayName("Flash allows Clique to champion and tap lands during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        championFaerie(faerie.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistbind Clique");
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        assertThat(island.isTapped()).isTrue();
    }
}
