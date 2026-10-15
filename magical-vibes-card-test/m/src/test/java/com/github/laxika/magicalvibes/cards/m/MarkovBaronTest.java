package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.f.FalkenrathGorger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkovBaron.class, BaronyVampire.class, GrizzlyBears.class, RavensCrime.class,
        FalkenrathGorger.class})
class MarkovBaronTest extends BaseCardTest {

    @Test
    void multipleBaronsBuffEachOtherAndStopBuffingWhenTheyLeave() {
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new MarkovBaron());
        List<Permanent> barons = findPermanents(player1, "Markov Baron");

        for (Permanent baron : barons) {
            assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(3);
        }

        gd.playerBattlefields.get(player1.getId()).remove(barons.get(1));

        assertThat(gqs.getEffectivePower(gd, barons.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barons.getFirst())).isEqualTo(2);
    }

    @Test
    void combatDamageGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MarkovBaron());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void convokeCanPayEntireNormalCostUsingSummoningSickCreatures() {
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new MarkovBaron());
        List<Permanent> sources = findPermanents(player1, "Markov Baron");
        MarkovBaron spell = new MarkovBaron();
        harness.setHand(player1, List.of(spell));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                sources.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(sources).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void discardExilesBaronAndCreatesMadnessTrigger() {
        MarkovBaron baron = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(baron.getId()));
        harness.assertNotInGraveyard(player1, "Markov Baron");
        assertThat(gd.stack).anyMatch(entry -> entry.getDescription().contains("madness"));
    }

    @Test
    void decliningMadnessPutsBaronIntoGraveyard() {
        discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Markov Baron");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void madnessCastsBaronDuringOpponentsTurnForThreeMana() {
        MarkovBaron baron = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(baron.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void madnessMustNotRejectCastWhenConvokeCanPayTheCost() {
        harness.addToBattlefield(player1, new FalkenrathGorger());
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new MarkovBaron());
        MarkovBaron baron = discardViaRavensCrime();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(baron.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Markov Baron");
    }

    private MarkovBaron discardViaRavensCrime() {
        MarkovBaron baron = new MarkovBaron();
        harness.setHand(player1, List.of(baron));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return baron;
    }

    @Test
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent vampire = findPermanent(player1, "Barony Vampire");

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MarkovBaron());

        Permanent baron = findPermanent(player1, "Markov Baron");

        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(2);
    }

    @Test
    void doesNotBuffNonVampiresOrOpponentsVampires() {
        harness.addToBattlefield(player1, new MarkovBaron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BaronyVampire());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentVampire = findPermanent(player2, "Barony Vampire");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
    }
}
