package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarkwaterCatacombs;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmobilizingInk.class, DuskImp.class, DarkwaterCatacombs.class})
class ImmobilizingInkTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addTappedCreature();
        Permanent otherCreature = addTappedCreature();
        addAura(creature);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can pay mana and discard a card to untap")
    void enchantedCreatureCanUntap() {
        Permanent creature = addTappedCreature();
        addAura(creature);
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dusk Imp");
    }

    @Test
    @DisplayName("Can cast Immobilizing Ink targeting a creature")
    void canCastOnCreature() {
        Permanent creature = addCreatureReady(player1, new DuskImp());
        harness.setHand(player1, List.of(new ImmobilizingInk()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Immobilizing Ink");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The enchanted creature's controller can activate the granted ability")
    void enchantedCreatureControllerCanActivateAbility() {
        Permanent creature = addTappedCreature(player2);
        addAura(player1, creature);
        harness.setHand(player2, List.of(new DuskImp()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("The untap lock follows an enchanted creature controlled by an opponent")
    void lockAppliesDuringEnchantedCreatureControllersUntapStep() {
        Permanent creature = addTappedCreature(player2);
        addAura(player1, creature);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura removes the granted ability")
    void effectsEndWhenAuraLeaves() {
        Permanent creature = addTappedCreature();
        Permanent aura = addAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Immobilizing Ink cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new DarkwaterCatacombs());
        harness.setHand(player1, List.of(new ImmobilizingInk()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate the untap ability without a card to discard")
    void cannotActivateWithEmptyHand() {
        Permanent creature = addTappedCreature();
        addAura(creature);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick creature can activate the untap ability")
    void summoningSickCreatureCanActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        creature.setSummoningSick(true);
        creature.tap();
        addAura(creature);
        harness.setHand(player1, List.of(new DarkwaterCatacombs()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Darkwater Catacombs");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated untap ability resolves after the Aura leaves")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent creature = addTappedCreature();
        Permanent otherCreature = addTappedCreature();
        Permanent aura = addAura(creature);
        harness.setHand(player1, List.of(new DuskImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura restores the creature's normal untap")
    void untapLockEndsWhenAuraLeaves() {
        Permanent creature = addTappedCreature();
        Permanent aura = addAura(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addTappedCreature() {
        return addTappedCreature(player1);
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = addCreatureReady(player, new DuskImp());
        creature.tap();
        return creature;
    }

    private Permanent addAura(Permanent enchantedCreature) {
        return addAura(player1, enchantedCreature);
    }

    private Permanent addAura(Player auraController,
                              Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new ImmobilizingInk());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
