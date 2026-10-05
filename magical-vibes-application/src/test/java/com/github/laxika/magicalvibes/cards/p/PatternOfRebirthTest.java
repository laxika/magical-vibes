package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FledglingOsprey;
import com.github.laxika.magicalvibes.cards.f.Flicker;
import com.github.laxika.magicalvibes.cards.s.ScentOfNightshade;
import com.github.laxika.magicalvibes.cards.t.TwistedExperiment;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatternOfRebirth.class, FledglingOsprey.class, PlatedSpider.class,
        ScentOfNightshade.class, TwistedExperiment.class, YavimayaHollow.class, Flicker.class})
class PatternOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature's controller may search for a creature onto the battlefield")
    void enchantedCreatureControllerMaySearch() {
        Permanent creature = attachAuraToCreature(player1, player2);
        Card foundCreature = new PlatedSpider();
        harness.setLibrary(player2, List.of(new YavimayaHollow(), foundCreature));

        killCreature(creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.CREATURE));

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(foundCreature.getId()));
    }

    @Test
    @DisplayName("The enchanted creature's controller may decline the search")
    void enchantedCreatureControllerMayDecline() {
        Permanent creature = attachAuraToCreature(player1, player2);
        Card foundCreature = new PlatedSpider();
        harness.setLibrary(player2, List.of(foundCreature));

        killCreature(creature);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(foundCreature.getId()));
    }

    @Test
    @DisplayName("Accepting the search does not put a noncreature card onto the battlefield")
    void searchWithNoCreatureInLibrary() {
        Permanent creature = attachAuraToCreature(player1, player2);
        harness.setLibrary(player2, List.of(new YavimayaHollow()));

        killCreature(creature);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature leaves without dying")
    void doesNotTriggerWhenEnchantedCreatureIsFlickered() {
        attachAuraToCreature(player1, player2);

        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Pattern of Rebirth");
        harness.assertOnBattlefield(player2, "Fledgling Osprey");
    }

    @Test
    @DisplayName("Pattern of Rebirth cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaHollow());

        harness.setHand(player1, List.of(new PatternOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Aura's controller controls the death trigger even on an opponent's creature")
    void auraControllerControlsDeathTrigger() {
        Permanent creature = attachAuraToCreature(player1, player2);
        creature.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Fledgling Osprey");
        harness.assertInGraveyard(player1, "Pattern of Rebirth");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A restricted search may fail to find even with a creature in the library")
    void mayFailToFindCreature() {
        Permanent creature = attachAuraToCreature(player1, player2);
        Card foundCreature = new PlatedSpider();
        harness.setLibrary(player2, List.of(foundCreature));

        killCreature(creature);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(foundCreature);
        harness.assertNotOnBattlefield(player2, "Plated Spider");
    }

    @Test
    @DisplayName("Accepting a search with an empty library completes without a card choice")
    void searchEmptyLibrary() {
        Permanent creature = attachAuraToCreature(player1, player2);
        harness.setLibrary(player2, List.of());

        killCreature(creature);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting Pattern of Rebirth attaches it and its controller can search after the creature dies")
    void castAuraAndSearchOwnLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FledglingOsprey());
        harness.setHand(player1, List.of(new PatternOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Card foundCreature = new PlatedSpider();
        harness.setLibrary(player1, List.of(foundCreature));

        killCreature(creature);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plated Spider");
        harness.assertInGraveyard(player1, "Pattern of Rebirth");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent attachAuraToCreature(Player auraController, Player creatureController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new FledglingOsprey());

        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new PatternOfRebirth());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        ScentOfNightshade killSpell = new ScentOfNightshade();
        TwistedExperiment blackCard = new TwistedExperiment();
        harness.setHand(player1, List.of(killSpell, blackCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));
        harness.passBothPriorities();
    }
}
