package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
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

@CardUsed({DarkLeoShredder.class, NinjaOfTheDeepHours.class, GrizzlyBears.class})
class DarkLeoShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Ninjas you control have deathtouch")
    void attackingNinjasYouControlHaveDeathtouch() {
        Permanent darkLeoShredder = addCreatureReady(player1, new DarkLeoShredder());
        Permanent ownNinja = addCreatureReady(player1, new NinjaOfTheDeepHours());
        Permanent ownNonNinja = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentNinja = addCreatureReady(player2, new NinjaOfTheDeepHours());

        darkLeoShredder.setAttacking(true);
        ownNinja.setAttacking(true);
        ownNonNinja.setAttacking(true);
        opponentNinja.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, darkLeoShredder, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNinja, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonNinja, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentNinja, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Combat damage creates a Ninja token but does not cause life loss below five Ninjas")
    void combatDamageCreatesNinjaWithoutConditionalLifeLoss() {
        harness.setLife(player2, 22);
        Permanent darkLeoShredder = addCreatureReady(player1, new DarkLeoShredder());
        darkLeoShredder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("The created Ninja counts toward the five-Ninja life-loss condition")
    void createdNinjaCountsTowardLifeLoss() {
        harness.setLife(player2, 22);
        addCreatureReady(player1, new NinjaOfTheDeepHours());
        addCreatureReady(player1, new NinjaOfTheDeepHours());
        addCreatureReady(player1, new NinjaOfTheDeepHours());
        Permanent darkLeoShredder = addCreatureReady(player1, new DarkLeoShredder());
        darkLeoShredder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Dark Leo & Shredder tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new DarkLeoShredder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent darkLeoShredder = findPermanent(player1, "Dark Leo & Shredder");
        assertThat(darkLeoShredder.isTapped()).isTrue();
        assertThat(darkLeoShredder.isAttacking()).isTrue();
        assertThat(darkLeoShredder.getAttackTarget()).isEqualTo(player2.getId());
    }
}
