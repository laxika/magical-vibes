package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BasrisSolidarity;
import com.github.laxika.magicalvibes.cards.i.InvigoratingSurge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HerdBaloth.class, BasrisSolidarity.class, InvigoratingSurge.class})
class HerdBalothTest extends BaseCardTest {

    @Test
    void createsBeastTokenWhenCounterIsPutOnIt() {
        Permanent herdBaloth = harness.addToBattlefieldAndReturn(player1, new HerdBaloth());
        castBasrisSolidarity();

        assertThat(herdBaloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).contains(CardSubtype.BEAST);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void decliningTriggerDoesNotCreateBeastToken() {
        Permanent herdBaloth = harness.addToBattlefieldAndReturn(player1, new HerdBaloth());
        castBasrisSolidarity();

        assertThat(herdBaloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void separatePlacementsTriggerTwiceEvenWhenOnePlacementAddsMultipleCounters() {
        Permanent herdBaloth = harness.addToBattlefieldAndReturn(player1, new HerdBaloth());
        herdBaloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new InvigoratingSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, herdBaloth.getId());

        assertThat(herdBaloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countersOnAnotherCreatureDoNotTriggerHerdBaloth() {
        harness.addToBattlefield(player1, new HerdBaloth());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HerdBaloth());
        harness.setHand(player2, List.of(new BasrisSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player2, 0, 0);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    private void castBasrisSolidarity() {
        harness.setHand(player1, List.of(new BasrisSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
