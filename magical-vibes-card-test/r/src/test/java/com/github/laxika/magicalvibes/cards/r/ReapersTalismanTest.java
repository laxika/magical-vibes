package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArmoryVeteran;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReapersTalisman.class, ArmoryVeteran.class})
class ReapersTalismanTest extends BaseCardTest {

    @Test
    void attackingAloneGivesDeathtouchAndDrainsDefendingPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new ArmoryVeteran());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new ArmoryVeteran());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void attackingWithAnotherCreatureOnlyGivesDeathtouch() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new ArmoryVeteran());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());
        addCreatureReady(player1, new ArmoryVeteran());

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void attackingAloneCreatesTwoSeparateTriggers() {
        Permanent creature = addCreatureReady(player1, new ArmoryVeteran());
        addTalismanReady(player1).setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void deathtouchStillGoesToAttackerAfterTalismanBecomesUnattached() {
        Permanent creature = addCreatureReady(player1, new ArmoryVeteran());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            talisman.setAttachedTo(null);
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void changingAttachmentDoesNotGiveDeathtouchToDifferentCreature() {
        Permanent attacker = addCreatureReady(player1, new ArmoryVeteran());
        Permanent talisman = addTalismanReady(player1);
        Permanent other = addCreatureReady(player1, new ArmoryVeteran());
        talisman.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            talisman.setAttachedTo(other.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void attackingWithAnUnequippedCreatureDoesNotTriggerTalisman() {
        Permanent equipped = addCreatureReady(player1, new ArmoryVeteran());
        addTalismanReady(player1).setAttachedTo(equipped.getId());
        Permanent attacker = addCreatureReady(player1, new ArmoryVeteran());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(2)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void equipAttachesToControlledCreatureForTwoMana() {
        Permanent creature = addCreatureReady(player1, new ArmoryVeteran());
        Permanent talisman = addTalismanReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(talisman.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addTalismanReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ReapersTalisman());
    }
}
