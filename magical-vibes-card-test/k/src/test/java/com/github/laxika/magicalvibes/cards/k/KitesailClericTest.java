package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.r.RelicAmulet;
import com.github.laxika.magicalvibes.cards.v.VanquishTheWeak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitesailCleric.class, CliffhavenSellSword.class, RelicAmulet.class, VanquishTheWeak.class})
class KitesailClericTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotTapCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void kickedTapsUpToTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void kickedMayChooseOnlyOneTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void kickedMayChooseNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedCannotTargetNoncreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new RelicAmulet());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(noncreature.isTapped()).isFalse();
    }

    @Test
    void kickedCanTargetItselfAndAnotherControlledCreature() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent cleric = findPermanent(player1, "Kitesail Cleric");
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.handlePermanentChosen(player1, other.getId());
        resolveAllTriggers();

        assertThat(cleric.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    void kickedCannotChooseTheSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void remainingTargetIsTappedWhenOtherTargetLeavesBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric(), new VanquishTheWeak()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first);
        assertThat(second.isTapped()).isFalse();
        resolveAllTriggers();

        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void abilityStillTapsTargetsAfterClericLeavesTheBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new KitesailCleric(), new VanquishTheWeak()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        var clericId = harness.getPermanentId(player1, "Kitesail Cleric");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, clericId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Kitesail Cleric");
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }
}
