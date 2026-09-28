package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TashaUnholyArchmage.class, GrizzlyBears.class, Shock.class, Forest.class})
class TashaUnholyArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts -1/-1 counters on creatures attacking you or Tasha")
    void plusOneTriggersForPlayerAndPlaneswalkerAttacks() {
        Permanent tasha = addReadyTasha(4);
        Permanent directAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalkerAttacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int directIndex = gd.playerBattlefields.get(player2.getId()).indexOf(directAttacker);
        int planeswalkerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(planeswalkerAttacker);
        gs.declareAttackers(gd, player2, List.of(directIndex, planeswalkerIndex),
                Map.of(directIndex, player1.getId(), planeswalkerIndex, tasha.getId()));
        resolveAllTriggers();

        assertThat(directAttacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(planeswalkerAttacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 returns an opponent's creature with ward {2}")
    void minusTwoReturnsCreatureWithWard() {
        addReadyTasha(4);
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creatureCard));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.WARD)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("-6 puts three revealed creatures onto your battlefield and the rest into the opponent's graveyard")
    void minusSixRevealsThreeCreaturesAndGraveyardsTheRest() {
        addReadyTasha(6);
        Card shock = new Shock();
        Card firstCreature = new GrizzlyBears();
        Card forest = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondShock = new Shock();
        Card thirdCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(shock, firstCreature, forest, secondCreature, secondShock, thirdCreature));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(shock, forest, secondShock);
    }

    private Permanent addReadyTasha(int loyalty) {
        Permanent permanent = new Permanent(new TashaUnholyArchmage());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
