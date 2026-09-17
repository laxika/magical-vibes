package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({MoonbladeShinobi.class, GrizzlyBears.class})
class MoonbladeShinobiTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying blue Illusion token when dealing combat damage to a player")
    void createsIllusionTokenOnCombatDamage() {
        Permanent shinobi = addCreatureReady(player1, new MoonbladeShinobi());
        shinobi.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        Permanent illusion = findPermanent(player1, "Illusion");
        assertThat(illusion.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, illusion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, illusion)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, illusion, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token when blocked")
    void blockedShinobiDoesNotCreateToken() {
        Permanent shinobi = addCreatureReady(player1, new MoonbladeShinobi());
        shinobi.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion")).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts the Shinobi onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MoonbladeShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent shinobi = findPermanent(player1, "Moonblade Shinobi");
        assertThat(shinobi.isTapped()).isTrue();
        assertThat(shinobi.isAttacking()).isTrue();
        assertThat(shinobi.getAttackTarget()).isEqualTo(player2.getId());
    }
}
