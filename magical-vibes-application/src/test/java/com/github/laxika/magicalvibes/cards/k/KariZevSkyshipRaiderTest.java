package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LeaveInTheDust;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KariZevSkyshipRaider.class, LeaveInTheDust.class})
class KariZevSkyshipRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a legendary Ragavan token tapped and attacking")
    void attackCreatesRagavanToken() {
        addCreatureReady(player1, new KariZevSkyshipRaider());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        Permanent ragavan = findPermanent(player1, "Ragavan");
        assertThat(ragavan.getCard().isToken()).isTrue();
        assertThat(ragavan.getCard().getPower()).isEqualTo(2);
        assertThat(ragavan.getCard().getToughness()).isEqualTo(1);
        assertThat(ragavan.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(ragavan.getCard().getSubtypes()).containsExactly(CardSubtype.MONKEY);
        assertThat(ragavan.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(ragavan.isTapped()).isTrue();
        assertThat(ragavan.isAttacking()).isTrue();
        assertThat(ragavan.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Ragavan is exiled at end of combat")
    void ragavanExiledAtEndOfCombat() {
        addCreatureReady(player1, new KariZevSkyshipRaider());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });
        Permanent ragavan = findPermanent(player1, "Ragavan");

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ragavan);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ragavan);
    }

    @Test
    @DisplayName("The attack trigger creates Ragavan even after Kari Zev leaves, and still exiles it")
    void sourceLeavingDoesNotStopTokenCreationOrExile() {
        Permanent kariZev = addCreatureReady(player1, new KariZevSkyshipRaider());
        harness.setHand(player2, List.of(new LeaveInTheDust()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.castAndResolveInstant(player2, 0, kariZev.getId());
            harness.assertInHand(player1, "Kari Zev, Skyship Raider");
            resolveAllTriggers();
        });

        Permanent ragavan = findPermanent(player1, "Ragavan");
        assertThat(ragavan.isAttacking()).isTrue();
        assertThat(ragavan.isTapped()).isTrue();
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ragavan);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ragavan);
    }

}
