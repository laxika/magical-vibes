package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinTrailblazer;
import com.github.laxika.magicalvibes.cards.s.SunCollaredRaptor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireFleetNeckbreaker.class, GoblinTrailblazer.class, SunCollaredRaptor.class})
class DireFleetNeckbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Pirates you control get +2/+0")
    void boostsAttackingPiratesYouControl() {
        Permanent neckbreaker = addCreatureReady(player1, new DireFleetNeckbreaker());
        Permanent attackingPirate = addCreatureReady(player1, createPirateCard("Attacking Pirate"));
        Permanent stayingPirate = addCreatureReady(player1, createPirateCard("Staying Pirate"));
        Permanent nonPirate = addCreatureReady(player1, createNonPirateCard("Non-Pirate"));
        Permanent opponentPirate = addCreatureReady(player2, createPirateCard("Opponent Pirate"));

        int neckbreakerPower = gqs.getEffectivePower(gd, neckbreaker);
        int attackingPiratePower = gqs.getEffectivePower(gd, attackingPirate);
        int stayingPiratePower = gqs.getEffectivePower(gd, stayingPirate);
        int nonPiratePower = gqs.getEffectivePower(gd, nonPirate);
        int opponentPiratePower = gqs.getEffectivePower(gd, opponentPirate);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, neckbreaker)).isEqualTo(neckbreakerPower + 2);
        assertThat(gqs.getEffectivePower(gd, attackingPirate)).isEqualTo(attackingPiratePower + 2);
        assertThat(gqs.getEffectivePower(gd, stayingPirate)).isEqualTo(stayingPiratePower);
        assertThat(gqs.getEffectivePower(gd, nonPirate)).isEqualTo(nonPiratePower);
        assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(opponentPiratePower);
    }

    @Test
    void boostsPirateWhileSourceStaysBackButNotAttackingDinosaur() {
        Permanent neckbreaker = addCreatureReady(player1, new DireFleetNeckbreaker());
        Permanent pirate = addCreatureReady(player1, new GoblinTrailblazer());
        Permanent dinosaur = addCreatureReady(player1, new SunCollaredRaptor());
        int sourcePower = gqs.getEffectivePower(gd, neckbreaker);
        int piratePower = gqs.getEffectivePower(gd, pirate);
        int pirateToughness = gqs.getEffectiveToughness(gd, pirate);
        int dinosaurPower = gqs.getEffectivePower(gd, dinosaur);

        declareAttackersAndPrepareBlockers(List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(piratePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(pirateToughness);
        assertThat(gqs.getEffectivePower(gd, neckbreaker)).isEqualTo(sourcePower);
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(dinosaurPower);

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            gs.declareBlockers(gd, player2, java.util.Map.of());
            harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        });

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(piratePower);
    }

    @Test
    void doesNotBoostOpponentsAttackingPirate() {
        addCreatureReady(player1, new DireFleetNeckbreaker());
        Permanent pirate = addCreatureReady(player2, new GoblinTrailblazer());
        int power = gqs.getEffectivePower(gd, pirate);

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(power);
    }

    @Test
    void multipleNeckbreakersBoostThemselvesAndEachOtherWhileAttacking() {
        Permanent first = addCreatureReady(player1, new DireFleetNeckbreaker());
        Permanent second = addCreatureReady(player1, new DireFleetNeckbreaker());
        Permanent pirate = addCreatureReady(player1, new GoblinTrailblazer());
        int firstPower = gqs.getEffectivePower(gd, first);
        int secondPower = gqs.getEffectivePower(gd, second);
        int piratePower = gqs.getEffectivePower(gd, pirate);
        int toughness = gqs.getEffectiveToughness(gd, first);

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(firstPower + 4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(secondPower + 4);
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(piratePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(toughness);
    }

    private Card createPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.PIRATE));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createNonPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.GOBLIN));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
