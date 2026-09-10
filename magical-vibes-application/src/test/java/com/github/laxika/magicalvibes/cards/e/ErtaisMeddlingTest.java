package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErtaisMeddling.class, LowlandGiant.class, LightningBlast.class})
class ErtaisMeddlingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving exiles the target spell with X delay counters on it")
    void exilesTargetSpellWithDelayCounters() {
        LowlandGiant giant = castGiantAndMeddle(2);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Lowland Giant");
        harness.assertNotInGraveyard(player1, "Lowland Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lowland Giant"));

        GameData.DelayedSpellExile pending = gd.delayedSpellExiles.getFirst();
        assertThat(pending.cardId()).isEqualTo(giant.getId());
        assertThat(pending.controllerId()).isEqualTo(player1.getId());
        assertThat(pending.counters()).isEqualTo(2);
    }

    @Test
    @DisplayName("The exiled spell's controller's upkeep removes one delay counter")
    void upkeepRemovesOneDelayCounter() {
        castGiantAndMeddle(2);

        triggerUpkeep(player1);

        assertThat(gd.delayedSpellExiles).singleElement()
                .extracting(GameData.DelayedSpellExile::counters).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lowland Giant"));
        harness.assertNotOnBattlefield(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("Only the exiled spell's controller's upkeep removes a delay counter")
    void opponentUpkeepDoesNotRemoveCounter() {
        castGiantAndMeddle(2);

        triggerUpkeep(player2);

        assertThat(gd.delayedSpellExiles).singleElement()
                .extracting(GameData.DelayedSpellExile::counters).isEqualTo(2);
    }

    @Test
    @DisplayName("When the last delay counter is removed the card goes back onto the stack and resolves")
    void lastCounterPutsSpellBackOntoTheStack() {
        castGiantAndMeddle(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the trigger — Lowland Giant goes back onto the stack

        assertThat(gd.delayedSpellExiles).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Lowland Giant"));
        assertThat(gd.stack).singleElement()
                .extracting(e -> e.getCard().getName()).isEqualTo("Lowland Giant");

        harness.passBothPriorities(); // resolve Lowland Giant

        harness.assertOnBattlefield(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("Two delay counters delay the spell until the controller's second upkeep")
    void twoCountersTakeTwoUpkeeps() {
        castGiantAndMeddle(2);

        triggerUpkeep(player1);
        harness.assertNotOnBattlefield(player1, "Lowland Giant");

        triggerUpkeep(player1);
        harness.passBothPriorities(); // resolve Lowland Giant

        harness.assertOnBattlefield(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("X cannot be zero when Ertai's Meddling is cast")
    void cannotCastWithZeroDelayCounters() {
        LowlandGiant giant = new LowlandGiant();
        harness.setHand(player1, List.of(giant));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The delayed spell is exiled in its owner's exile zone")
    void exilesTargetSpellToItsOwnersExileZone() {
        LowlandGiant giant = new LowlandGiant();
        harness.setHand(player1, List.of(giant));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(giant.getId()))
                .findFirst()
                .orElseThrow()
                .setOwnerIdOverride(player2.getId());

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(giant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("A delayed targeted spell keeps its original target")
    void delayedTargetedSpellKeepsItsOriginalTarget() {
        LightningBlast blast = new LightningBlast();
        harness.setHand(player1, List.of(blast));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, blast.getId());
        harness.passBothPriorities();

        int lifeBeforeBlastResolves = gd.getLife(player2.getId());
        triggerUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeBlastResolves - 4);
    }

    @Test
    @DisplayName("A copy targeted in exile ceases to exist instead of being delayed")
    void copyTargetCeasesToExistInExile() {
        LowlandGiant giant = new LowlandGiant();
        harness.setHand(player1, List.of(giant));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(giant.getId()))
                .findFirst()
                .orElseThrow()
                .setCopy(true);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.delayedSpellExiles).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(giant.getId()));
        harness.assertNotInGraveyard(player1, "Lowland Giant");
        harness.assertNotOnBattlefield(player1, "Lowland Giant");
    }

    @Test
    @DisplayName("Resolving fizzles if the targeted spell is no longer on the stack")
    void fizzlesIfTargetSpellLeavesTheStack() {
        LowlandGiant giant = new LowlandGiant();
        harness.setHand(player1, List.of(giant));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, giant.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(giant.getId()));

        harness.passBothPriorities();

        assertThat(gd.delayedSpellExiles).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(giant.getId()));
        harness.assertInGraveyard(player2, "Ertai's Meddling");
    }

    /**
     * player1 casts Lowland Giant, player2 responds with Ertai's Meddling for the given X and both
     * players let the Meddling resolve.
     */
    private LowlandGiant castGiantAndMeddle(int xValue) {
        LowlandGiant giant = new LowlandGiant();
        harness.setHand(player1, List.of(giant));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, xValue);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, xValue, giant.getId());
        harness.passBothPriorities();

        return giant;
    }

    private void triggerUpkeep(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities(); // resolve the triggered ability
    }
}
