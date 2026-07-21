package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnquenchableThirstTest extends BaseCardTest {

    // ===== ETB tap =====

    @Test
    @DisplayName("ETB taps enchanted creature when you control a Desert")
    void etbTapsWithDesertOnBattlefield() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));

        Permanent bears = addCreatureReady(player2);

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities(); // resolve the Aura — ETB trigger onto stack
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB taps enchanted creature when a Desert card is in your graveyard")
    void etbTapsWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));

        Permanent bears = addCreatureReady(player2);

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap without any Desert, but Aura still attaches")
    void etbNoTapWithoutDesert() {
        Permanent bears = addCreatureReady(player2);

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities(); // resolve the Aura — intervening-if fails, no trigger queued

        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Unquenchable Thirst") && p.isAttached());
    }

    // ===== Doesn't untap =====

    @Test
    @DisplayName("Tapped enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent aura = new Permanent(new UnquenchableThirst());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again after Unquenchable Thirst is removed")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();

        Permanent aura = new Permanent(new UnquenchableThirst());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

        Permanent desert = new Permanent(new SunscorchedDesert());
        gd.playerBattlefields.get(player1.getId()).add(desert);

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, desert.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
