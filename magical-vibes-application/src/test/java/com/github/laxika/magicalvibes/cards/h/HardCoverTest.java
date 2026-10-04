package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HardCover.class, GreenwoodSentinel.class, Forest.class, Mountain.class})
class HardCoverTest extends BaseCardTest {

    @Test
    @DisplayName("Hard Cover gives the enchanted creature +0/+2")
    void boostsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HardCover());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature can tap to draw then discard")
    void enchantedCreatureCanLoot() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HardCover());
        aura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Hard Cover cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HardCover()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The drawn card can be chosen for discard")
    void canDiscardNewlyDrawnCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HardCover());
        aura.setAttachedTo(creature.getId());
        Mountain original = new Mountain();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("An opposing enchanted creature loots for its controller")
    void opposingCreatureControllerLoots() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new HardCover()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        Mountain original = new Mountain();
        Forest drawn = new Forest();
        harness.setHand(player2, List.of(original));
        harness.setLibrary(player2, List.of(drawn));

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(original, drawn);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(original);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the granted tap ability")
    void summoningSicknessPreventsLooting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HardCover());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
    }
}
