package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClamorShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Riot can put a +1/+1 counter on Clamor Shaman")
    void riotAddsCounter() {
        Permanent shaman = castShaman();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shaman.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Declining Riot gives Clamor Shaman haste")
    void riotGivesHasteWhenDeclined() {
        Permanent shaman = castShaman();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shaman.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Attacking targets only a creature an opponent controls")
    void attackTriggerRestrictsTargets() {
        Permanent shaman = addCreatureReady(player1, new ClamorShaman());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId())
                .doesNotContain(shaman.getId(), ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
    }

    private Permanent castShaman() {
        harness.setHand(player1, List.of(new ClamorShaman()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
