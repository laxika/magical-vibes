package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CatharsCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible when its controller casts a noncreature spell")
    void gainsIndestructibleForNoncreatureSpell() {
        harness.addToBattlefield(player1, new CatharsCompanion());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent companion = findPermanent(player1, "Cathar's Companion");
        UUID companionId = companion.getId();
        harness.castInstant(player1, 0, companionId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, companion, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(companion);
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new CatharsCompanion());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent companion = findPermanent(player1, "Cathar's Companion");
        assertThat(gqs.hasKeyword(gd, companion, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CatharsCompanion());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent companion = findPermanent(player1, "Cathar's Companion");
        harness.castInstant(player1, 0, companion.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, companion, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
