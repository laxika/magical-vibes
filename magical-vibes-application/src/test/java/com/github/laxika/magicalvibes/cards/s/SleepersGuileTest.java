package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleepersGuile.class, GiantCockroach.class, Crawlspace.class})
class SleepersGuileTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sleeper's Guile attaches it and gives the enchanted creature fear")
    void resolvingAttachesAndGrantsFear() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sleeper's Guile");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Sleeper's Guile can enchant a creature controlled by an opponent")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sleeper's Guile");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Sleeper's Guile gives fear only to the enchanted creature")
    void grantsFearOnlyToEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Sleeper's Guile returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent aura = new Permanent(new SleepersGuile());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sleeper's Guile");
        harness.assertNotInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotOnBattlefield(player1, "Sleeper's Guile");
    }

    @Test
    @DisplayName("Sleeper's Guile returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByOpponent() {
        SleepersGuile card = new SleepersGuile();
        card.setOwnerId(player1.getId());
        Permanent aura = new Permanent(card);
        gd.playerBattlefields.get(player2.getId()).add(aura);
        gd.stolenCreatures.put(aura.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sleeper's Guile");
        harness.assertNotInHand(player2, "Sleeper's Guile");
        harness.assertNotInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotOnBattlefield(player2, "Sleeper's Guile");
    }

    @Test
    @DisplayName("Sleeper's Guile cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
