package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltraMagnusTactician.class, UltraMagnusArmoredCarrier.class, GrizzlyBears.class,
        HillGiant.class, Ornithopter.class, GiantGrowth.class, Shock.class})
class UltraMagnusTacticianTest extends BaseCardTest {

    @Test
    void attackPutsArtifactCreatureTappedAndAttackingAndConvertsAtEndOfCombat() {
        Permanent magnus = addCreatureReady(player1, new UltraMagnusTactician());
        harness.setHand(player1, List.of(new Ornithopter()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
        assertThat(ornithopter.isAttacking()).isTrue();
        assertThat(ornithopter.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(magnus.isTransformed()).isFalse();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(magnus.isTransformed()).isTrue();
        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusArmoredCarrier.class);
    }

    @Test
    void decliningAttackTriggerDoesNotConvert() {
        Permanent magnus = addCreatureReady(player1, new UltraMagnusTactician());
        harness.setHand(player1, List.of(new Ornithopter()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(magnus.isTransformed()).isFalse();
        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusTactician.class);
    }

    @Test
    void armoredCarrierGrantsIndestructibleButDoesNotConvertBelowFormidablePower() {
        Permanent magnus = castConvertedUltraMagnus();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, magnus, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusArmoredCarrier.class);
    }

    @Test
    void armoredCarrierConvertsWhenAttackingCreaturesHaveTotalPowerAtLeastEight() {
        Permanent magnus = castConvertedUltraMagnus();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, magnus, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(magnus.isTransformed()).isFalse();
        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusTactician.class);
    }

    @Test
    void formidableChecksPowerAfterAResponseIncreasesIt() {
        Permanent magnus = castConvertedUltraMagnus();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.castAndResolveInstant(player1, 0, bears.getId());
            resolveAllTriggers();
        });

        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusTactician.class);
        assertThat(gqs.hasKeyword(gd, magnus, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void formidableDoesNotConvertAfterAnAttackerDiesInResponse() {
        Permanent magnus = castConvertedUltraMagnus();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            harness.ensurePriority(player2);
            harness.castAndResolveInstant(player2, 0, bears.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusArmoredCarrier.class);
        assertThat(gqs.hasKeyword(gd, magnus, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void attackWithNoArtifactCreatureInHandDoesNotConvert() {
        Permanent magnus = addCreatureReady(player1, new UltraMagnusTactician());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(magnus.getCard()).isInstanceOf(UltraMagnusTactician.class);
    }

    @Test
    void indestructibleExcludesNonattackersAndExpiresAtEndOfTurn() {
        Permanent magnus = castConvertedUltraMagnus();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new HillGiant());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, magnus, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.isCreature(gd, magnus)).isFalse();
    }

    private Permanent castConvertedUltraMagnus() {
        harness.setHand(player1, List.of(new UltraMagnusTactician()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ultra Magnus, Armored Carrier");
    }
}
