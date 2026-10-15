package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.g.GelidShackles;
import com.github.laxika.magicalvibes.cards.h.HibernationsEnd;
import com.github.laxika.magicalvibes.cards.p.PhyrexianEtchings;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurTheEnchanter.class, BorealDruid.class, GelidShackles.class,
        HibernationsEnd.class, PhyrexianEtchings.class})
class ZurTheEnchanterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a may search prompt")
    void attackingCreatesMaySearchPrompt() {
        addReadyZur();
        harness.setLibrary(player1, List.of(new PhyrexianEtchings()));

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the attack trigger puts an eligible enchantment onto the battlefield")
    void acceptingSearchPutsEnchantmentOntoBattlefield() {
        addReadyZur();
        Card enchantment = new PhyrexianEtchings();
        harness.setLibrary(player1, List.of(enchantment));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Phyrexian Etchings");
        assertThat(gameData.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the attack trigger does not search")
    void decliningSearchDoesNothing() {
        addReadyZur();
        harness.setLibrary(player1, List.of(new PhyrexianEtchings()));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Phyrexian Etchings");
    }

    @Test
    @DisplayName("The search offers only enchantments with mana value 3 or less")
    void searchFiltersByTypeAndManaValue() {
        addReadyZur();
        harness.setLibrary(player1, List.of(
                new PhyrexianEtchings(),
                new HibernationsEnd(),
                new BorealDruid()));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Phyrexian Etchings");
    }

    @Test
    @DisplayName("Accepting the trigger with no eligible enchantment completes without a search")
    void acceptingSearchWithNoEligibleEnchantmentDoesNothing() {
        addReadyZur();
        HibernationsEnd tooExpensive = new HibernationsEnd();
        BorealDruid creature = new BorealDruid();
        harness.setLibrary(player1, List.of(tooExpensive, creature));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooExpensive, creature);
        harness.assertNotOnBattlefield(player1, "Hibernation's End");
        harness.assertNotOnBattlefield(player1, "Boreal Druid");
    }

    @Test
    @DisplayName("A restricted search may fail to find even with an eligible enchantment")
    void mayFailToFindEligibleEnchantment() {
        addReadyZur();
        Card enchantment = new PhyrexianEtchings();
        harness.setLibrary(player1, List.of(enchantment));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
        harness.assertNotOnBattlefield(player1, "Phyrexian Etchings");
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("A searched Aura enters attached to a chosen opposing creature")
    void searchedAuraEntersAttachedToOpposingCreature() {
        addReadyZur();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorealDruid());
        harness.setLibrary(player1, List.of(new GelidShackles()));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Gelid Shackles").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotOnBattlefield(player2, "Gelid Shackles");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting the trigger with an empty library completes and shuffles")
    void acceptingSearchWithEmptyLibraryCompletes() {
        addReadyZur();
        harness.setLibrary(player1, List.of());

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("The attack trigger still searches after Zur leaves the battlefield")
    void attackTriggerResolvesAfterZurLeavesBattlefield() {
        addReadyZur();
        Permanent zur = findPermanent(player1, "Zur the Enchanter");
        harness.setLibrary(player1, List.of(new PhyrexianEtchings()));

        declareAttack();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, zur);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Zur the Enchanter");
        harness.assertOnBattlefield(player1, "Phyrexian Etchings");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A searched Aura stays in the library when no legal creature remains")
    void searchedAuraWithNoLegalHostStaysInLibrary() {
        addReadyZur();
        Permanent zur = findPermanent(player1, "Zur the Enchanter");
        Card aura = new GelidShackles();
        harness.setLibrary(player1, List.of(aura));

        declareAttack();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, zur);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player1, "Gelid Shackles");
        harness.assertNotInGraveyard(player1, "Gelid Shackles");
    }

    private void addReadyZur() {
        addCreatureReady(player1, new ZurTheEnchanter());
    }

    private void declareAttack() {
        declareAttackers(player1, List.of(0));
    }
}
