package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WickedWolf;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodrageAlpha.class, WickedWolf.class, WyluliWolf.class, GrizzlyBears.class, Ornithopter.class})
class BloodrageAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("The fight mode makes another Wolf or Werewolf you control fight an opposing creature")
    void fightMode() {
        Permanent wolf = addCreatureReady(player1, new WickedWolf());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        castBloodrage(0, List.of(wolf.getId(), opponent.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(wolf.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boon grants the next Wolf or Werewolf spell an enter-the-battlefield fight with up to one target")
    void boonGrantsNextMatchingSpellFight() {
        Permanent opponent = addCreatureReady(player2, new Ornithopter());
        chooseBoonMode();

        harness.setHand(player1, List.of(new WyluliWolf(), new WyluliWolf()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        assertThat(opponent.getMarkedDamage()).isEqualTo(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A boon-granted entry ability goes on the stack even with no opposing creatures")
    void boonEntryWithoutLegalTargetsStillTriggers() {
        chooseBoonMode();
        harness.setHand(player1, List.of(new WyluliWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wyluli Wolf");
    }

    @Test
    @DisplayName("Casting a non-Wolf creature does not consume the boon")
    void nonWolfSpellDoesNotConsumeBoon() {
        Permanent opponent = addCreatureReady(player2, new Ornithopter());
        chooseBoonMode();
        harness.setHand(player1, List.of(new GrizzlyBears(), new WyluliWolf()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponent.getMarkedDamage()).isZero();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    private void chooseBoonMode() {
        castBloodrage(1, List.of());
        resolveAllTriggers();
    }

    private void castBloodrage(int mode, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BloodrageAlpha()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, mode, targetIds);
    }
}
