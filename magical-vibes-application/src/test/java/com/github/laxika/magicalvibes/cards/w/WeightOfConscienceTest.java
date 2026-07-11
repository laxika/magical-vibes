package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightOfConscienceTest extends BaseCardTest {

    // ===== Static: enchanted creature can't attack =====

    @Test
    @DisplayName("Creature enchanted with Weight of Conscience cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new WeightOfConscience());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(auraPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Activated ability: tap two creatures sharing a type, exile enchanted creature =====

    @Test
    @DisplayName("Tapping two creatures that share a creature type exiles the enchanted creature")
    void activatedAbilityExilesEnchantedCreature() {
        Permanent enchanted = new Permanent(new GrizzlyBears());
        enchanted.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(enchanted);

        Permanent auraPerm = new Permanent(new WeightOfConscience());
        auraPerm.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        Permanent bear2 = new Permanent(new GrizzlyBears());
        bear2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);
        gd.playerBattlefields.get(player1.getId()).add(bear2);

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(auraPerm);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        // Both Bears (shared creature type) tapped to pay the cost
        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();

        // Enchanted creature exiled, and the now-orphaned Aura leaves the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(enchanted.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Weight of Conscience"));
    }

    // ===== Activated ability illegal when no two creatures share a type =====

    @Test
    @DisplayName("Ability can't be activated without two creatures sharing a creature type")
    void cannotActivateWithoutSharedType() {
        Permanent enchanted = new Permanent(new GrizzlyBears());
        enchanted.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(enchanted);

        Permanent auraPerm = new Permanent(new WeightOfConscience());
        auraPerm.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Bear + Elf: no shared creature type
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        Permanent elf = new Permanent(new LlanowarElves());
        elf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);
        gd.playerBattlefields.get(player1.getId()).add(elf);

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(auraPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a creature type");

        // Enchanted creature untouched
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
    }
}
