package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StanggEchoWarrior.class, Rancor.class, Bonesplitter.class})
class StanggEchoWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking legendary Stangg Twin")
    void attackingCreatesTwin() {
        addCreatureReady(player1, new StanggEchoWarrior());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();

            Permanent twin = findPermanents(player1, "Stangg Twin").getFirst();
            assertThat(twin.getCard().getPower()).isEqualTo(3);
            assertThat(twin.getCard().getToughness()).isEqualTo(4);
            assertThat(twin.isTapped()).isTrue();
            assertThat(twin.isAttacking()).isTrue();
        });
    }

    @Test
    @DisplayName("Copies each Aura and Equipment attached to Stangg onto the Twin")
    void copiesAttachedAurasAndEquipment() {
        Permanent stangg = addCreatureReady(player1, new StanggEchoWarrior());
        Permanent rancor = harness.addToBattlefieldAndReturn(player1, new Rancor());
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        rancor.setAttachedTo(stangg.getId());
        bonesplitter.setAttachedTo(stangg.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent twin = findPermanents(player1, "Stangg Twin").getFirst();
        Permanent rancorCopy = findPermanents(player1, "Rancor").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        Permanent bonesplitterCopy = findPermanents(player1, "Bonesplitter").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(rancorCopy.getAttachedTo()).isEqualTo(twin.getId());
        assertThat(bonesplitterCopy.getAttachedTo()).isEqualTo(twin.getId());
        assertThat(rancor.getAttachedTo()).isEqualTo(stangg.getId());
        assertThat(bonesplitter.getAttachedTo()).isEqualTo(stangg.getId());
        assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(7);
    }

    @Test
    @DisplayName("The Twin and copied attachments are sacrificed at the next end step")
    void sacrificesTokensAtNextEndStep() {
        Permanent stangg = addCreatureReady(player1, new StanggEchoWarrior());
        Permanent rancor = harness.addToBattlefieldAndReturn(player1, new Rancor());
        rancor.setAttachedTo(stangg.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Stangg Twin")).hasSize(1);
        assertThat(findPermanents(player1, "Rancor")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Stangg Twin")).isEmpty();
        assertThat(findPermanents(player1, "Rancor")).containsExactly(rancor);
    }
}
