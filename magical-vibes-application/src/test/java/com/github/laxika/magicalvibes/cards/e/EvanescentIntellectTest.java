package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvanescentIntellectTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap and pay mana to mill three cards from a target player")
    void enchantedCreatureMillsTargetPlayer() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(creature);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability can target its controller")
    void enchantedCreatureCanMillItsController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(creature);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void abilityCannotTargetPermanent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(creature);
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Aura can enchant only a creature")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new EvanescentIntellect()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent forest = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addAttachedAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EvanescentIntellect());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
