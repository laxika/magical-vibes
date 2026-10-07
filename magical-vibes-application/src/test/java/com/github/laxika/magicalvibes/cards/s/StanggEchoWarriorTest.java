package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.DaybreakCoronet;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StanggEchoWarrior.class, Rancor.class, Bonesplitter.class,
        DaybreakCoronet.class, DoublingSeason.class, Murder.class})
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

    @Test
    @DisplayName("Copies the attachments Stangg had immediately before leaving the battlefield")
    void copiesAttachmentsUsingLastKnownInformation() {
        Permanent stangg = addCreatureReady(player1, new StanggEchoWarrior());
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        bonesplitter.setAttachedTo(stangg.getId());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castAndResolveInstant(player1, 0, stangg.getId());
            harness.assertNotOnBattlefield(player1, "Stangg, Echo Warrior");
            resolveAllTriggers();

            Permanent twin = findPermanents(player1, "Stangg Twin").getFirst();
            List<Permanent> equipmentCopies = findPermanents(player1, "Bonesplitter").stream()
                    .filter(permanent -> permanent.getCard().isToken()).toList();
            assertThat(equipmentCopies).hasSize(1);
            assertThat(equipmentCopies.getFirst().getAttachedTo()).isEqualTo(twin.getId());
            assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(5);
            assertThat(bonesplitter.getAttachedTo()).isNull();
        });
    }

    @Test
    @DisplayName("Doubled Equipment copies enter attached before the legend rule is applied")
    void doubledAttachmentsAreCreatedBeforeLegendChoice() {
        Permanent stangg = addCreatureReady(player1, new StanggEchoWarrior());
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        bonesplitter.setAttachedTo(stangg.getId());
        harness.addToBattlefield(player1, new DoublingSeason());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();

            List<Permanent> twins = findPermanents(player1, "Stangg Twin");
            List<Permanent> equipmentCopies = findPermanents(player1, "Bonesplitter").stream()
                    .filter(permanent -> permanent.getCard().isToken()).toList();
            assertThat(twins).hasSize(2);
            assertThat(equipmentCopies).hasSize(2);
            assertThat(equipmentCopies).allSatisfy(copy ->
                    assertThat(copy.getAttachedTo()).isIn(twins.stream().map(Permanent::getId).toList()));
        });
    }

    @Test
    @DisplayName("An Aura that cannot enchant the Twin is not created")
    void cannotUseSimultaneouslyEnteringAuraToSatisfyCoronetEnchantRestriction() {
        Permanent stangg = addCreatureReady(player1, new StanggEchoWarrior());
        Permanent rancor = harness.addToBattlefieldAndReturn(player1, new Rancor());
        Permanent coronet = harness.addToBattlefieldAndReturn(player1, new DaybreakCoronet());
        rancor.setAttachedTo(stangg.getId());
        coronet.setAttachedTo(stangg.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();

            Permanent twin = findPermanents(player1, "Stangg Twin").getFirst();
            assertThat(findPermanents(player1, "Daybreak Coronet")).containsExactly(coronet);
            assertThat(findPermanents(player1, "Rancor")).hasSize(2);
            assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(5);
        });
    }
}
