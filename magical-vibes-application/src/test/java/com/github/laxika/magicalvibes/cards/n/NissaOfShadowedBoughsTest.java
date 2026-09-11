package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissaOfShadowedBoughs.class, Forest.class, Swamp.class, GrizzlyBears.class, HillGiant.class})
class NissaOfShadowedBoughsTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a loyalty counter on Nissa")
    void landfallPutsLoyaltyCounterOnNissa() {
        Permanent nissa = addReadyNissa(player1, 3);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 untaps a land and optionally animates it with haste and menace")
    void plusOneUntapsAndAnimatesLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        forest.tap();

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.MENACE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("+1 may leave the land unanimated")
    void plusOneMayDeclineAnimation() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        forest.tap();

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("-5 puts a qualifying creature from hand or graveyard onto the battlefield with two counters")
    void minusFivePutsCreatureWithTwoCounters() {
        addReadyNissa(player1, 5);
        addLand(player1);
        addLand(player1);
        Card creature = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setHand(player1, List.of(tooExpensive));
        harness.setGraveyard(player1, List.of(creature));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent permanent = new Permanent(new NissaOfShadowedBoughs());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addLand(Player player) {
        Permanent permanent = new Permanent(player == player1 ? new Forest() : new Swamp());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
