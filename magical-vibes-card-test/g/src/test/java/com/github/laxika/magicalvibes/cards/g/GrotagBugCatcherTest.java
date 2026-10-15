package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrotagBugCatcher.class, SoulWarden.class, FaerieMiscreant.class, FugitiveWizard.class,
        ExpeditionHealer.class, ExpeditionDiviner.class, StoneworkPackbeast.class, IntoTheRoil.class})
class GrotagBugCatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when it attacks as the only party creature")
    void countsItselfAsWarrior() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +4/+0 when it attacks with a full party")
    void boostsByPartySize() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new FugitiveWizard());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void duplicateWarriorsCountOnlyOnce() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new GrotagBugCatcher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void opposingCreaturesDoNotJoinYourParty() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player2, new ExpeditionHealer());
        harness.addToBattlefield(player2, new ExpeditionDiviner());
        harness.addToBattlefield(player2, new StoneworkPackbeast());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void creatureWithAllPartyTypesCountsAsOnlyOneMember() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new StoneworkPackbeast());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void flexiblePartyMemberFillsTheMissingRole() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ExpeditionDiviner());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void partyIsCountedWhenAttackTriggerResolves() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, healer.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Expedition Healer");
        harness.assertNotOnBattlefield(player1, "Expedition Healer");
        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void resolvedBoostDoesNotShrinkWhenPartyMemberLeaves() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new IntoTheRoil()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });
        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(3);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, healer.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Expedition Healer");
        harness.assertNotOnBattlefield(player1, "Expedition Healer");
        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    void boostedAttackerTramplesOverOneToughnessBlocker() {
        addCreatureReady(player1, new GrotagBugCatcher());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new StoneworkPackbeast());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Stonework Packbeast");
        harness.assertInGraveyard(player1, "Grotag Bug-Catcher");
    }
}
