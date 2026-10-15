package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BubbleSnare.class, TazeemRoilmage.class, Island.class, IntoTheRoil.class})
class BubbleSnareTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotTapButPreventsUntapping() {
        Permanent creature = addCreatureReady(player2, new TazeemRoilmage());

        castBubbleSnare(creature);

        assertThat(creature.isTapped()).isFalse();
        creature.tap();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void withKickerTapsTheEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new TazeemRoilmage());
        harness.setHand(player1, List.of(new BubbleSnare()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void canTargetOnlyAcreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new BubbleSnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castBubbleSnare(Permanent target) {
        harness.setHand(player1, List.of(new BubbleSnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    @Test
    void kickedTriggerStillTapsAfterAuraIsReturnedToHand() {
        Permanent creature = addCreatureReady(player2, new TazeemRoilmage());
        harness.setHand(player1, List.of(new BubbleSnare()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Bubble Snare");
        assertThat(creature.isTapped()).isFalse();
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        resolveAllTriggers();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void creatureUntapsNormallyAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new TazeemRoilmage());
        castBubbleSnare(creature);
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        Permanent aura = findPermanent(player1, "Bubble Snare");
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }
}
