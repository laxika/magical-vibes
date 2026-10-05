package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InstillEnergy;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MijaeDjinn.class, GrizzlyBears.class, InstillEnergy.class, Unsummon.class})
class MijaeDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("On attack, a lost coin flip removes and taps the Djinn")
    void lostCoinFlipRemovesAndTapsSource() {
        Permanent djinn = addCreatureReady(player1, new MijaeDjinn());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        boolean wonFlip = gameLogContains("wins the coin flip for Mijae Djinn");
        boolean lostFlip = gameLogContains("loses the coin flip for Mijae Djinn");
        assertThat(wonFlip).isNotEqualTo(lostFlip);
        assertThat(djinn.isAttacking()).isEqualTo(wonFlip);
        assertThat(djinn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A lost flip taps a Djinn untapped in response; a win leaves it untapped and attacking")
    void untappedAttackerIsTappedOnlyOnLoss() {
        Permanent djinn = addCreatureReady(player1, new MijaeDjinn());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        aura.setAttachedTo(djinn.getId());
        addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gameLogContains("coin flip for Mijae Djinn")).isFalse();

            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
            assertThat(djinn.isTapped()).isFalse();
            assertThat(djinn.isAttacking()).isTrue();

            resolveAllTriggers();

            boolean wonFlip = gameLogContains("wins the coin flip for Mijae Djinn");
            boolean lostFlip = gameLogContains("loses the coin flip for Mijae Djinn");
            assertThat(wonFlip).isNotEqualTo(lostFlip);
            assertThat(djinn.isAttacking()).isEqualTo(wonFlip);
            assertThat(djinn.isTapped()).isEqualTo(lostFlip);
        });
    }

    @Test
    @DisplayName("The attack trigger still flips after its source leaves without affecting another creature")
    void sourceLeavingDoesNotPreventFlipOrAffectAnotherPermanent() {
        Permanent djinn = addCreatureReady(player1, new MijaeDjinn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castAndResolveInstant(player1, 0, djinn.getId());
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(djinn);
            assertThat(gameLogContains("coin flip for Mijae Djinn")).isFalse();

            resolveAllTriggers();

            boolean wonFlip = gameLogContains("wins the coin flip for Mijae Djinn");
            boolean lostFlip = gameLogContains("loses the coin flip for Mijae Djinn");
            assertThat(wonFlip).isNotEqualTo(lostFlip);
            assertThat(bears.isTapped()).isFalse();
            assertThat(bears.isAttacking()).isFalse();
            assertThat(gd.playerHands.get(player1.getId())).contains(djinn.getCard());
        });
    }
}
