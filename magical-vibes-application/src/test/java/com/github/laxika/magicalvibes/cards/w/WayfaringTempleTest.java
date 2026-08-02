package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WayfaringTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures its controller controls")
    void powerAndToughnessEqualControlledCreatureCount() {
        Permanent temple = addReadyTemple();

        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(1);

        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, temple)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, temple)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a player triggers populate")
    void combatDamageTriggersPopulate() {
        Permanent temple = addReadyTemple();
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        temple.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
    }

    @Test
    @DisplayName("Populate does nothing when its combat damage is prevented by a blocker")
    void blockedCombatDamageDoesNotTriggerPopulate() {
        Permanent temple = addReadyTemple();
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent blocker = new Permanent(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        temple.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
    }

    private Permanent addReadyTemple() {
        Permanent temple = new Permanent(new WayfaringTemple());
        temple.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(temple);
        return temple;
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
