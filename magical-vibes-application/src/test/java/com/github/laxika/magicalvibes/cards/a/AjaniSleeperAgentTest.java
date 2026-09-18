package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniSleeperAgent.class, GrizzlyBears.class, ChandraNalaar.class, LightningBolt.class})
class AjaniSleeperAgentTest extends BaseCardTest {

    @Test
    void plusOnePutsCreatureIntoHand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOnePutsPlaneswalkerIntoHand() {
        harness.setLibrary(player1, List.of(new ChandraNalaar()));
        addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof ChandraNalaar);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOneCanPutNonmatchingCardOnBottom() {
        GrizzlyBears cardBelow = new GrizzlyBears();
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard, cardBelow));
        addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBelow, topCard);
    }

    @Test
    void minusThreeDistributesCountersAndGrantsVigilance() {
        addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.ensurePriority(player1);
        harness.getGameService().activateAbility(
                gd,
                player1,
                0,
                1,
                null,
                null,
                null,
                List.of(first.getId(), second.getId(), third.getId()),
                Map.of(first.getId(), 1, second.getId(), 1, third.getId(), 1)
        );
        harness.passBothPriorities();

        assertThat(first.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(second.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(third.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void emblemPoisonsOpponentWhenCreatureIsCast() {
        addReadyAjani(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getDescription().contains("Ajani, Sleeper Agent's emblem"));
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void emblemDoesNotTriggerForNoncreatureSpell() {
        addReadyAjani(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Ajani, Sleeper Agent's emblem"));
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent ajani = new Permanent(new AjaniSleeperAgent());
        ajani.setCounterCount(CounterType.LOYALTY, loyalty);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ajani);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ajani;
    }
}
