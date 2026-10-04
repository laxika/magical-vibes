package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RinoaAngelWing.class, GrizzlyBears.class, EkunduGriffin.class})
class RinoaAngelWingTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Rinoa boosts flying creatures and grants vigilance")
    void boostsFlyingCreaturesAndGrantsVigilance() {
        addCreatureReady(player1, new RinoaAngelWing());
        Permanent flyer = addCreatureReady(player1, new EkunduGriffin());
        Permanent groundCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, groundCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, groundCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Returns one attacking ally from a simultaneous death and only triggers once each turn")
    void returnsOneAttackingAllyAndIsOncePerTurn() {
        Permanent rinoa = addCreatureReady(player1, new RinoaAngelWing());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        firstAttacker.setMarkedDamage(2);
        secondAttacker.setMarkedDamage(2);
        nonAttacker.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstAttacker.getCard().getId(), secondAttacker.getCard().getId());

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(secondAttacker.getCard()));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(secondAttacker.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstAttacker.getCard().getId(), nonAttacker.getCard().getId());

        Permanent laterAttacker = addCreatureReady(player1, new GrizzlyBears());
        laterAttacker.setAttacking(true);
        laterAttacker.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(laterAttacker.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rinoa);
    }
}
