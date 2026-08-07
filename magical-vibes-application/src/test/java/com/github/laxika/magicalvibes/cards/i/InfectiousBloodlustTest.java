package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

class InfectiousBloodlustTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and has haste")
    void enchantedCreatureGetsBoostAndHaste() {
        Permanent creature = addCreatureWithAura(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4); // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature must attack each combat if able")
    void enchantedCreatureMustAttack() {
        Permanent creature = addCreatureWithAura(player1);
        creature.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Haste means even a summoning-sick enchanted creature is forced to attack")
    void summoningSickEnchantedCreatureStillMustAttack() {
        addCreatureWithAura(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Accepting the death trigger fetches another copy into hand")
    void deathTriggerFetchesAnotherCopy() {
        Permanent creature = addCreatureWithAura(player1);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new InfectiousBloodlust());
        deck.add(new GrizzlyBears());

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Infectious Bloodlust");
    }

    @Test
    @DisplayName("Declining the death trigger does not search the library")
    void decliningSkipsSearch() {
        Permanent creature = addCreatureWithAura(player1);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new InfectiousBloodlust());

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Infectious Bloodlust"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent fountain = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new InfectiousBloodlust()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void killEnchantedCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the death trigger → may prompt
    }

    /**
     * Places a Grizzly Bears (2/2) on the given player's battlefield with an
     * Infectious Bloodlust attached, both controlled by that player.
     *
     * @return the Grizzly Bears permanent
     */
    private Permanent addCreatureWithAura(Player controller) {
        harness.addToBattlefield(controller, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(controller.getId()).getFirst();

        Permanent aura = new Permanent(new InfectiousBloodlust());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);

        return creature;
    }
}
