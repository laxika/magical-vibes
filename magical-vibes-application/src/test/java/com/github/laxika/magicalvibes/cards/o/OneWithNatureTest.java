package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OneWithNature.class, GrizzlyBears.class, Forest.class, Plains.class})
class OneWithNatureTest extends BaseCardTest {

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

    private Permanent attachOneWithNature(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new OneWithNature());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void setupLibraryWithBasicLands() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Plains()));
    }
}
