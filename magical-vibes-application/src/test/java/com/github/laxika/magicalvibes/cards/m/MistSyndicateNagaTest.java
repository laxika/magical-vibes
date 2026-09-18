package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistSyndicateNaga.class, GrizzlyBears.class})
class MistSyndicateNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of itself when dealing combat damage to a player")
    void createsTokenCopyOnCombatDamage() {
        Permanent naga = addCreatureReady(player1, new MistSyndicateNaga());
        naga.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Mist-Syndicate Naga"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts the Naga in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MistSyndicateNaga()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent naga = findPermanent(player1, "Mist-Syndicate Naga");
        assertThat(naga.isTapped()).isTrue();
        assertThat(naga.isAttacking()).isTrue();
        assertThat(naga.getAttackTarget()).isEqualTo(player2.getId());
    }
}
