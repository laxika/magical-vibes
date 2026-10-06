package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IroncladKrovod;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamutsSprint.class, IroncladKrovod.class, Mountain.class})
class SamutsSprintTest extends BaseCardTest {

    @Test
    void boostsGivesHasteAndScriesOne() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new IroncladKrovod());
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        castSamutsSprint(bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostAndHasteWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new IroncladKrovod());
        harness.setLibrary(player1, List.of());
        castSamutsSprint(bear.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SamutsSprint()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canKeepTheScryedCardOnTop() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IroncladKrovod());
        Card topCard = new Mountain();
        Card nextCard = new SamutsSprint();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        castSamutsSprint(creature.getId());

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOpponentsCreatureAndScryCastersLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IroncladKrovod());
        Card topCard = new Mountain();
        Card nextCard = new SamutsSprint();
        Card opponentsCard = new IroncladKrovod();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentsCard));
        castSamutsSprint(creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotScryWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IroncladKrovod());
        Card topCard = new Mountain();
        Card nextCard = new IroncladKrovod();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new SamutsSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Samut's Sprint");
    }

    @Test
    void hasteLetsASummoningSickCreatureAttackWithAnEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IroncladKrovod());
        creature.setSummoningSick(true);
        harness.setLibrary(player1, List.of());
        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();

        castSamutsSprint(creature.getId());

        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castSamutsSprint(UUID targetId) {
        harness.setHand(player1, List.of(new SamutsSprint()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
