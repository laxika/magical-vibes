package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.Brushland;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OneWithNature.class, GrizzlyBears.class, Forest.class, Plains.class, Brushland.class})
class OneWithNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Casting One with Nature attaches it to a creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OneWithNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof OneWithNature
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Combat damage from the enchanted creature creates a may search")
    void combatDamageCreatesMaySearch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachOneWithNature(player1, creature);
        setupLibraryWithBasicLands();
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the combat-damage ability puts a basic land onto the battlefield tapped")
    void acceptingCombatDamageAbilitySearchesForTappedBasicLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachOneWithNature(player1, creature);
        setupLibraryWithBasicLands();
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = findPermanent(player1, chosenName);
        assertThat(chosenLand.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search offers only basic lands")
    void searchOffersOnlyBasicLands() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachOneWithNature(player1, creature);
        Brushland nonbasicLand = new Brushland();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(nonbasicLand, forest, plains));
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, plains);

        Card chosenLand = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, chosenLand.getName()).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonbasicLand).doesNotContain(chosenLand);
    }

    @Test
    @DisplayName("Declining the combat-damage ability does not search")
    void decliningCombatDamageAbilityDoesNotSearch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachOneWithNature(player1, creature);
        setupLibraryWithBasicLands();
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player1, "Plains")).isEmpty();
    }

    @Test
    @DisplayName("Accepting the ability with no basic land does not prompt")
    void acceptingWithNoBasicLandDoesNotPrompt() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachOneWithNature(player1, creature);
        Brushland nonbasicLand = new Brushland();
        harness.setLibrary(player1, List.of(nonbasicLand));
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player1, "Plains")).isEmpty();
    }

    @Test
    @DisplayName("The Aura's controller chooses and searches their library")
    void auraControllerChoosesAndSearchesTheirLibrary() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        attachOneWithNature(player1, opponentCreature);
        Forest forest = new Forest();
        Plains plains = new Plains();
        Brushland opponentLand = new Brushland();
        harness.setLibrary(player1, List.of(forest, plains));
        harness.setLibrary(player2, List.of(opponentLand));
        opponentCreature.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().cards()).containsExactly(forest, plains);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    @DisplayName("A blocked enchanted creature does not trigger the Aura")
    void blockedEnchantedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachOneWithNature(player1, creature);
        setupLibraryWithBasicLands();
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void attachOneWithNature(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new OneWithNature());
        aura.setAttachedTo(creature.getId());
    }

    private void setupLibraryWithBasicLands() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
    }
}
