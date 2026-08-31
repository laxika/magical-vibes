package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GadwicksFirstDuel.class, GrizzlyBears.class, Concentrate.class, Forest.class, Shock.class})
class GadwicksFirstDuelTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a Cursed Role attached to any target creature")
    void chapterICreatesCursedRoleAttachedToAnyCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Cursed");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II scries 2")
    void chapterIIScriesTwo() {
        addSagaWithLore(1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToNextChapter();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Chapter III copies the next instant or sorcery with mana value 3 or less")
    void chapterIIICopiesNextSmallInstantOrSorcery() {
        addSagaWithLore(2);
        advanceToNextChapter();

        harness.setHand(player1, List.of(new GrizzlyBears(), new Concentrate(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new GadwicksFirstDuel());
        Permanent saga = findPermanent(player1, "Gadwick's First Duel");
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
