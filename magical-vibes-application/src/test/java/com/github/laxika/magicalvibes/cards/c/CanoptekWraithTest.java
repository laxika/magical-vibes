package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanoptekWraith.class, Plains.class, Forest.class})
class CanoptekWraithTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepPriorityForResponses() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
    }


    @Test
    @DisplayName("Combat damage offers the payment and sacrifice ability")
    void combatDamageOffersPayment() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Paying and sacrificing searches up to two basic lands with the chosen land's name")
    void payingSacrificesAndSearchesSameNameBasics() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(
                new Plains(), new Plains(), new Forest(), new CanoptekWraith()));

        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canoptek Wraith");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, plains.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Plains", "Plains");
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Plains")))
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Plains"))
                .filter(permanent -> permanent != plains))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment keeps Canoptek Wraith on the battlefield")
    void decliningKeepsWraith() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Canoptek Wraith");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void cannotBeBlocked() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        Permanent blocker = addCreatureReady(player2, new CanoptekWraith());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, blocker, wraith, gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void canFindZeroLandsEvenWhenMatchingBasicsExist() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);
        harness.addToBattlefield(player1, new Plains());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));

        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Canoptek Wraith");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void insufficientManaDoesNotSacrificeWraithOrSearch() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);
        harness.addToBattlefield(player1, new Plains());
        harness.setLibrary(player1, List.of(new Plains()));

        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Canoptek Wraith");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void noLandsStillShufflesAfterPayingAndSacrificing() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        resolveCombatToMayPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Canoptek Wraith");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().toLowerCase().contains("shuffled"));
    }

    @Test
    void cannotSpendManaWhenSourceLeftBeforeTriggerResolved() {
        Permanent wraith = addCreatureReady(player1, new CanoptekWraith());
        wraith.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(wraith);
        gd.playerGraveyards.get(player1.getId()).add(wraith.getCard());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void resolveCombatToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
