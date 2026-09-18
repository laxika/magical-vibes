package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({YurikoTheTigersShadow.class, GrizzlyBears.class, HillGiant.class})
class YurikoTheTigersShadowTest extends BaseCardTest {

    @Test
    @DisplayName("A Ninja's combat damage reveals the top card, puts it into hand, and drains each opponent")
    void ninjaCombatDamageRevealsTopCardAndDrainsOpponent() {
        Card topCard = new HillGiant();
        addCreatureReady(player1, new YurikoTheTigersShadow());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Yuriko onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new YurikoTheTigersShadow()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent yuriko = findPermanent(player1, "Yuriko, the Tiger's Shadow");
        assertThat(yuriko.isTapped()).isTrue();
        assertThat(yuriko.isAttacking()).isTrue();
        assertThat(yuriko.getAttackTarget()).isEqualTo(player2.getId());
    }
}
