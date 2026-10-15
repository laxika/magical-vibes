package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RedSunsZenith;
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

@CardUsed({GadwicksFirstDuel.class, GrizzlyBears.class, Concentrate.class, Forest.class, Shock.class,
        RedSunsZenith.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Chapter I may choose no target even when a creature is available")
    void chapterICanChooseNoTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Cursed")).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering the battlefield triggers Chapter I and replaces an older Role you control")
    void enteringSagaReplacesOlderCursedRole() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        Permanent oldRole = findPermanent(player1, "Cursed");

        harness.setHand(player1, List.of(new GadwicksFirstDuel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cursed")).hasSize(1);
        Permanent newRole = findPermanent(player1, "Cursed");
        assertThat(newRole.getId()).isNotEqualTo(oldRole.getId());
        assertThat(newRole.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III allows a new target for the copy and only copies one spell")
    void chapterIIICanRetargetAndOnlyCopiesOnce() {
        addSagaWithLore(2);
        advanceToNextChapter();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Chapter III copies an X spell with mana value exactly three, preserving X")
    void chapterIIICopiesXSpellAtManaValueThree() {
        addSagaWithLore(2);
        advanceToNextChapter();
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Chapter III excludes an X spell with mana value above three without consuming its trigger")
    void chapterIIIDoesNotCopyXSpellAboveManaValueThree() {
        addSagaWithLore(2);
        advanceToNextChapter();
        harness.setHand(player1, List.of(new RedSunsZenith(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
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
