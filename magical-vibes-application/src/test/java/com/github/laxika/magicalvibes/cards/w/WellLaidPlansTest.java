package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WellLaidPlansTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Permanent addCreature(UUID controllerId, String name, int power, int toughness, CardColor color) {
        Permanent permanent = new Permanent(createCreature(name, power, toughness, color));
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(controllerId).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Prevents noncombat damage from a creature with a shared color")
    void preventsNoncombatDamageWithSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = addCreature(player2.getId(), "Red Target", 3, 3, CardColor.RED);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows noncombat damage when the creatures do not share a color")
    void allowsNoncombatDamageWithoutSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = addCreature(player2.getId(), "Green Target", 3, 3, CardColor.GREEN);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage between creatures with a shared color")
    void preventsCombatDamageWithSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        Permanent blocker = addCreature(player1.getId(), "Green Blocker", 2, 3, CardColor.GREEN);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreature(player2.getId(), "Green Attacker", 2, 2, CardColor.GREEN);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows combat damage when the creatures do not share a color")
    void allowsCombatDamageWithoutSharedColor() {
        harness.addToBattlefield(player1, new WellLaidPlans());
        Permanent blocker = addCreature(player1.getId(), "Green Blocker", 2, 3, CardColor.GREEN);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreature(player2.getId(), "Red Attacker", 1, 1, CardColor.RED);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }
}
