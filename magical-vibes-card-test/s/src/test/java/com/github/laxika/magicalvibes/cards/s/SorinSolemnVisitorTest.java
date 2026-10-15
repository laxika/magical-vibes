package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LuxiorGiadasGift;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SorinSolemnVisitor.class, GrizzlyBears.class})
class SorinSolemnVisitorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 boosts your creatures and grants lifelink until your next turn")
    void plusOneBoostsOwnCreaturesUntilNextTurn() {
        Permanent sorin = addReadySorin(player1, 3);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        ownCreature.clearUntilNextTurnEffects();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("-2 creates a 2/2 black Vampire creature token with flying")
    void minusTwoCreatesFlyingVampireToken() {
        Permanent sorin = addReadySorin(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Vampire");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("-6 creates an emblem that triggers during each opponent's upkeep")
    void minusSixCreatesOpponentUpkeepEmblem() {
        Permanent sorin = addReadySorin(player1, 6);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    private Permanent addReadySorin(Player player, int loyalty) {
        Permanent sorin = harness.addToBattlefieldAndReturn(player, new SorinSolemnVisitor());
        sorin.setCounterCount(CounterType.LOYALTY, loyalty);
        sorin.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sorin;
    }

    @Test
    @CardUsed({LuxiorGiadasGift.class})
    void plusOneGrantsLifelinkToSorinWhenHeIsACreature() {
        Permanent sorin = addReadySorin(player1, 4);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LuxiorGiadasGift());
        equipment.setAttachedTo(sorin.getId());
        assertThat(gqs.isCreature(gd, sorin)).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sorin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sorin)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sorin, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void plusOneOnlyAffectsCreaturesPresentAtResolutionAndSurvivesSorinLeaving() {
        Permanent sorin = addReadySorin(player1, 4);
        Permanent earlyCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(sorin);
        gd.playerGraveyards.get(player1.getId()).add(sorin.getCard());
        harness.passBothPriorities();
        Permanent lateCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, earlyCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, earlyCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.LIFELINK)).isFalse();

        gd.expireFloatingEffectsAtTurnStart(player2.getId());
        assertThat(gqs.getEffectivePower(gd, earlyCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, earlyCreature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void emblemLetsOpponentChooseAndTriggersAgainWithNoCreatures() {
        addReadySorin(player1, 6);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sorin, Solemn Visitor");

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
