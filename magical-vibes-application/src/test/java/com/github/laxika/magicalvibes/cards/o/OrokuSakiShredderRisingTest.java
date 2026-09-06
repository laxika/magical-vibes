package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrokuSakiShredderRising.class, Forest.class, GrizzlyBears.class})
class OrokuSakiShredderRisingTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws a card and makes Oroku Saki's controller lose 1 life")
    void combatDamageDrawsAndLosesLife() {
        Permanent saki = addReadySaki();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        saki.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and puts Oroku Saki onto the battlefield tapped and attacking")
    void sneaksOntoTheBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new OrokuSakiShredderRising()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == OrokuSakiShredderRising.class
                        && permanent.isTapped()
                        && permanent.isAttacking()
                        && permanent.getAttackTarget().equals(player2.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getClass)
                .doesNotContain(OrokuSakiShredderRising.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getClass)
                .contains(GrizzlyBears.class);
    }

    private Permanent addReadySaki() {
        Permanent saki = harness.addToBattlefieldAndReturn(player1, new OrokuSakiShredderRising());
        saki.setSummoningSick(false);
        return saki;
    }
}
