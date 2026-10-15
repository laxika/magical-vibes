package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessImpulse;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaformHellkite.class, Capsize.class, GrizzlyBears.class, Shock.class, RecklessImpulse.class})
class ManaformHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an X/X Dragon Illusion where X is mana spent to cast a noncreature spell")
    void createsTokenSizedToManaSpent() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon Illusion");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature spell does not trigger Manaform Hellkite")
    void creatureSpellDoesNotCreateToken() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon Illusion")).isEmpty();
    }

    @Test
    @DisplayName("The Dragon Illusion is exiled at the beginning of the next end step")
    void tokenIsExiledAtNextEndStep() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(findPermanents(player1, "Dragon Illusion")).hasSize(1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Dragon Illusion")).isEmpty();
    }

    @Test
    void opponentNoncreatureSpellDoesNotCreateToken() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon Illusion")).isEmpty();
        assertThat(findPermanents(player2, "Dragon Illusion")).isEmpty();
    }

    @Test
    void sorceryCreatesTokenBeforeSpellResolves() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.setHand(player1, List.of(new RecklessImpulse()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon Illusion")).hasSize(1);
        Permanent token = findPermanent(player1, "Dragon Illusion");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void tokenCreatedDuringEndStepWaitsForNextEndStep() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Dragon Illusion")).hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon Illusion")).isEmpty();
    }

    @Test
    void pendingTriggersKeepTheirOwnManaSpent() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Capsize(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithBuyback(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dragon Illusion"))
                .extracting(token -> gqs.getEffectivePower(gd, token))
                .containsExactlyInAnyOrder(1, 6);
        assertThat(findPermanents(player1, "Dragon Illusion"))
                .extracting(token -> gqs.getEffectiveToughness(gd, token))
                .containsExactlyInAnyOrder(1, 6);
    }

    @Test
    void triggerResolvesAfterHellkiteLeavesBattlefield() {
        addCreatureReady(player1, new ManaformHellkite());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Manaform Hellkite"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Manaform Hellkite")).isEmpty();
        assertThat(findPermanents(player1, "Dragon Illusion")).hasSize(1);
        Permanent token = findPermanent(player1, "Dragon Illusion");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(findPermanents(player2, "Dragon Illusion")).isEmpty();
    }
}
