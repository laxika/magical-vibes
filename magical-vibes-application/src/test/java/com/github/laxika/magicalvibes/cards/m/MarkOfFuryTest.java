package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Caltrops;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkOfFury.class, PlatedSpider.class, Caltrops.class})
class MarkOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has haste")
    void enchantedCreatureHasHaste() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkOfFury());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the beginning of the end step")
    void returnsItselfToHandAtEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkOfFury());
        aura.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mark of Fury");
        harness.assertInHand(player1, "Mark of Fury");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Returns the Aura to its owner's hand when another player controls it")
    void returnsAuraToOwnerHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        MarkOfFury auraCard = new MarkOfFury();
        auraCard.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, auraCard);
        aura.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(auraCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(auraCard);
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());
        MarkOfFury aura = new MarkOfFury();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == aura)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Caltrops());
        harness.setHand(player1, List.of(new MarkOfFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
