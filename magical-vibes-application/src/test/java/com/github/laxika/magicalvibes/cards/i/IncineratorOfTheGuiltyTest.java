package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncineratorOfTheGuilty.class, GrizzlyBears.class, NicolBolasPlaneswalker.class, SerraAngel.class})
class IncineratorOfTheGuiltyTest extends BaseCardTest {

    @Test
    void collectsEvidenceAndDamagesCreaturesAndPlaneswalkersThePlayerControls() {
        Card evidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidence));
        Permanent incinerator = addCreatureReady(player1, new IncineratorOfTheGuilty());
        incinerator.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel, bolas);
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void mayDeclineCollectingEvidence() {
        Permanent incinerator = addCreatureReady(player1, new IncineratorOfTheGuilty());
        incinerator.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    void mayCollectZeroEvidence() {
        Permanent incinerator = addCreatureReady(player1, new IncineratorOfTheGuilty());
        incinerator.setAttacking(true);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }
}
