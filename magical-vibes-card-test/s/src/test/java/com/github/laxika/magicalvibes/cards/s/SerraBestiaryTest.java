package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.a.AysenAbbey;
import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraBestiary.class, AnabaShaman.class, AysenAbbey.class, BeastWalkers.class})
class SerraBestiaryTest extends BaseCardTest {

    private void attachSerraBestiary(Player auraController, Permanent enchanted) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(auraController, new SerraBestiary());
        auraPerm.setAttachedTo(enchanted.getId());
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        attachSerraBestiary(player2, walkers);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new BeastWalkers());
        attachSerraBestiary(player1, blocker);

        addCreatureReady(player1, new BeastWalkers());
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate an ability with {T} in its cost")
    void enchantedCreatureCannotActivateTapAbility() {
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        attachSerraBestiary(player2, shaman);

        Permanent target = addCreatureReady(player2, new BeastWalkers());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature can still activate a non-tap ability")
    void enchantedCreatureCanActivateNonTapAbility() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        attachSerraBestiary(player2, walkers);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, walkers, Keyword.BANDING)).isTrue();
        assertThat(walkers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {W}{W} sacrifices Serra Bestiary")
    void decliningPaymentSacrificesAura() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        attachSerraBestiary(player1, walkers);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Serra Bestiary");
        harness.assertInGraveyard(player1, "Serra Bestiary");
    }

    @Test
    @DisplayName("Aura controller sacrifices Serra Bestiary when it enchants an opponent's creature")
    void auraControllerSacrificesAuraWhenEnchantedCreatureIsOpponent() {
        Permanent walkers = addCreatureReady(player2, new BeastWalkers());
        attachSerraBestiary(player1, walkers);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Serra Bestiary");
        harness.assertInGraveyard(player1, "Serra Bestiary");
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Serra Bestiary")
    void acceptingWithoutEnoughManaSacrificesAura() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        attachSerraBestiary(player1, walkers);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Serra Bestiary");
        harness.assertInGraveyard(player1, "Serra Bestiary");
    }

    @Test
    @DisplayName("Paying {W}{W} keeps Serra Bestiary on the battlefield")
    void payingKeepsAura() {
        Permanent walkers = addCreatureReady(player2, new BeastWalkers());
        attachSerraBestiary(player1, walkers);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Serra Bestiary");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        attachSerraBestiary(player1, walkers);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Bestiary");
    }

    @Test
    @DisplayName("Can target a creature with Serra Bestiary")
    void canTargetCreature() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        harness.setHand(player1, List.of(new SerraBestiary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, walkers.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Serra Bestiary")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AysenAbbey());
        harness.setHand(player1, List.of(new SerraBestiary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Serra Bestiary attaches it and prevents the creature from attacking")
    void resolvingAuraRestrictsEnchantedCreature() {
        Permanent walkers = addCreatureReady(player1, new BeastWalkers());
        harness.setHand(player1, List.of(new SerraBestiary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, walkers.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Bestiary").getAttachedTo()).isEqualTo(walkers.getId());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("One white and one green mana cannot pay the upkeep cost")
    void wrongColorsCannotPayUpkeep() {
        Permanent walkers = addCreatureReady(player2, new BeastWalkers());
        attachSerraBestiary(player1, walkers);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Serra Bestiary");
        harness.assertOnBattlefield(player2, "Beast Walkers");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Serra Bestiary restores the enchanted creature's tap ability")
    void sacrificingAuraRestoresTapAbility() {
        Permanent shaman = addCreatureReady(player1, new AnabaShaman());
        attachSerraBestiary(player1, shaman);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Anaba Shaman");
        assertThat(shaman.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }
}
