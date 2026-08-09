package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatternOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature's controller may search for a creature onto the battlefield")
    void enchantedCreatureControllerMaySearch() {
        Permanent creature = attachAuraToCreature(player1, player2);
        Card foundCreature = new GrizzlyBears();
        setLibrary(player2, List.of(new Forest(), foundCreature));

        killCreature(creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.CREATURE));

        harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(foundCreature.getId()));
    }

    @Test
    @DisplayName("The enchanted creature's controller may decline the search")
    void enchantedCreatureControllerMayDecline() {
        Permanent creature = attachAuraToCreature(player1, player2);
        Card foundCreature = new GrizzlyBears();
        setLibrary(player2, List.of(foundCreature));

        killCreature(creature);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(foundCreature.getId()));
    }

    @Test
    @DisplayName("Pattern of Rebirth cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.setHand(player1, List.of(new PatternOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachAuraToCreature(Player auraController, Player creatureController) {
        harness.addToBattlefield(creatureController, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).getFirst();

        Permanent aura = new Permanent(new PatternOfRebirth());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return creature;
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
