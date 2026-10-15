package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SublimeArchangel.class, WalkingCorpse.class, Murder.class})
class SublimeArchangelTest extends BaseCardTest {

    @Test
    void multipleArchangelsGrantSeparateInstancesToEachCreature() {
        addCreatureReady(player1, new SublimeArchangel());
        addCreatureReady(player1, new SublimeArchangel());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(2));
            assertThat(gd.stack).hasSize(6);
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(8);
    }

    @Test
    void opponentsLoneAttackerDoesNotReceiveYourExaltedBonus() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent attacker = addCreatureReady(player2, new WalkingCorpse());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            assertThat(gd.stack).isEmpty();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    void eachExaltedInstanceTriggersSeparately() {
        Permanent angel = addCreatureReady(player1, new SublimeArchangel());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack).extracting(entry -> entry.getSourcePermanentId())
                    .containsExactlyInAnyOrder(angel.getId(), attacker.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void removingArchangelInResponseDoesNotReduceExistingExaltedBonuses() {
        Permanent angel = addCreatureReady(player1, new SublimeArchangel());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.castAndResolveInstant(player2, 0, angel.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Sublime Archangel");
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void enteringCreatureAfterAttackDoesNotAddAnExaltedBonus() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.enterBattlefieldAndReturn(player1, new WalkingCorpse());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void opposingCreaturesDoNotContributeExalted() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new SublimeArchangel());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Lone attacker gets +2/+2 with only the Archangel alongside it")
    void twoCreaturesGivesTwoInstances() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1)); // Walking Corpse attacks alone
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each other creature you control adds another exalted instance (+3/+3 with three creatures)")
    void threeCreaturesGivesThreeInstances() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nothing triggers when more than one creature attacks")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new SublimeArchangel());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Sublime Archangel"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Archangel attacking alone gets the boost too")
    void archangelAttackingAlone() {
        Permanent angel = addCreatureReady(player1, new SublimeArchangel());
        addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
    }
}
