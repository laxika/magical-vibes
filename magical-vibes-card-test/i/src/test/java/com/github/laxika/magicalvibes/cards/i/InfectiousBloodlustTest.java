package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlchemistsVial;
import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.u.UnholyHunger;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({InfectiousBloodlust.class, DwynensElite.class, UnholyHunger.class, AlchemistsVial.class})
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
        harness.setLibrary(player1, List.of(new InfectiousBloodlust(), new DwynensElite()));

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Infectious Bloodlust");
    }

    @Test
    @DisplayName("Declining the death trigger does not search the library")
    void decliningSkipsSearch() {
        Permanent creature = addCreatureWithAura(player1);
        harness.setLibrary(player1, List.of(new InfectiousBloodlust()));

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Infectious Bloodlust");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AlchemistsVial());
        harness.setHand(player1, List.of(new InfectiousBloodlust()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting the Aura grants its abilities to an opposing creature")
    void canEnchantOpposingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwynensElite());
        harness.setHand(player1, List.of(new InfectiousBloodlust()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Death of an opposing enchanted creature searches the Aura controller's library")
    void opposingCreatureDeathSearchesAuraControllersLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwynensElite());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InfectiousBloodlust());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new InfectiousBloodlust()));
        harness.setLibrary(player2, List.of(new DwynensElite()));

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Infectious Bloodlust");
        harness.assertNotInHand(player2, "Infectious Bloodlust");
        harness.assertInGraveyard(player1, "Infectious Bloodlust");
    }

    @Test
    @DisplayName("A named-card search may fail to find even when a copy is present")
    void mayFailToFindExistingCopy() {
        Permanent creature = addCreatureWithAura(player1);
        harness.setHand(player1, List.of());
        InfectiousBloodlust copy = new InfectiousBloodlust();
        harness.setLibrary(player1, List.of(copy));

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Infectious Bloodlust");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(copy);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library completes without taking a card")
    void emptyLibrarySearchCompletes() {
        Permanent creature = addCreatureWithAura(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        killEnchantedCreature(creature);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInHand(player1, "Infectious Bloodlust");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped enchanted creature is not required to attack")
    void tappedCreatureMayStayOutOfCombat() {
        Permanent creature = addCreatureWithAura(player1);
        creature.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    private void killEnchantedCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new UnholyHunger()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Unholy Hunger — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the death trigger → may prompt
    }

    /**
     * Places a Dwynen's Elite (2/2) on the given player's battlefield with an
     * Infectious Bloodlust attached, both controlled by that player.
     *
     * @return the Dwynen's Elite permanent
     */
    private Permanent addCreatureWithAura(Player controller) {
        Permanent creature = harness.addToBattlefieldAndReturn(controller, new DwynensElite());

        Permanent aura = harness.addToBattlefieldAndReturn(controller, new InfectiousBloodlust());
        aura.setAttachedTo(creature.getId());

        return creature;
    }
}
