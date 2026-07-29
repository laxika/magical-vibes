package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarbedFoliageTest extends BaseCardTest {

    /** Puts Barbed Foliage on player1's battlefield and the given attacker on player2's. */
    private Permanent setUpAttack(Permanent attacker) {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new BarbedFoliage()));

        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return attacker;
    }

    @Test
    @DisplayName("A flanking attacker loses flanking until end of turn")
    void flankingAttackerLosesFlanking() {
        Permanent attacker = setUpAttack(new Permanent(new MtendaHerder()));
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isTrue();

        gs.declareAttackers(gd, player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("A non-flying attacker takes 1 damage")
    void nonFlyingAttackerTakesOneDamage() {
        Permanent attacker = setUpAttack(new Permanent(new GrizzlyBears()));

        gs.declareAttackers(gd, player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A flying attacker takes no damage but still loses flanking")
    void flyingAttackerTakesNoDamage() {
        Permanent attacker = setUpAttack(new Permanent(new AirElemental()));

        gs.declareAttackers(gd, player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isFalse();
    }
}
