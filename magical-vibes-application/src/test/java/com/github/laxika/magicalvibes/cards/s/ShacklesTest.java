package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shackles.class, RagingGoblin.class, Spellbook.class})
class ShacklesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = enchantOpponentCreature();
        creature.tap();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability returns Shackles to its owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        enchantOpponentCreature();
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Shackles"));

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shackles");
        harness.assertInHand(player1, "Shackles");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        addCreatureReady(player2, new RagingGoblin());

        harness.setHand(player1, List.of(new Shackles()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returning Shackles removes its untap restriction")
    void returningSelfRemovesUntapRestriction() {
        Permanent creature = enchantOpponentCreature();
        creature.tap();

        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Shackles"));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returns to its owner's hand when another player controls Shackles")
    void returnsToOwnersHandWhenOpponentControlsAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Shackles ownedShackles = new Shackles();
        ownedShackles.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, ownedShackles);
        aura.setAttachedTo(creature.getId());
        gd.stolenCreatures.put(aura.getId(), player1.getId());

        harness.addMana(player2, ManaColor.WHITE, 1);
        int auraIndex = gd.playerBattlefields.get(player2.getId()).indexOf(aura);
        harness.activateAbility(player2, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shackles");
        harness.assertInHand(player1, "Shackles");
        harness.assertNotInHand(player2, "Shackles");
    }

    private Permanent enchantOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        harness.setHand(player1, List.of(new Shackles()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Shackles");
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        return creature;
    }
}
