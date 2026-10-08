package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GishathSunsAvatar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalwartSpeartail.class, FutureSight.class, GishathSunsAvatar.class, GrizzlyBears.class,
        QuintoriusKand.class, Xenograft.class})
class StalwartSpeartailTest extends BaseCardTest {

    @Test
    void enragePerpetuallyBoostsOtherControlledDinosaursInAllSpecifiedZones() {
        Permanent source = addCreatureReady(player1, new StalwartSpeartail());
        Permanent otherDinosaur = addCreatureReady(player1, new GishathSunsAvatar());
        Permanent opponentDinosaur = addCreatureReady(player2, new GishathSunsAvatar());
        StalwartSpeartail handDinosaur = new StalwartSpeartail();
        StalwartSpeartail libraryDinosaur = new StalwartSpeartail();
        GrizzlyBears handBear = new GrizzlyBears();
        harness.setHand(player1, List.of(handDinosaur, handBear));
        harness.setLibrary(player1, List.of(libraryDinosaur));
        harness.addToBattlefield(player1, new FutureSight());

        int otherPowerBefore = gqs.getEffectivePower(gd, otherDinosaur);
        int otherToughnessBefore = gqs.getEffectiveToughness(gd, otherDinosaur);
        int opponentPowerBefore = gqs.getEffectivePower(gd, opponentDinosaur);
        int opponentToughnessBefore = gqs.getEffectiveToughness(gd, opponentDinosaur);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherDinosaur)).isEqualTo(otherPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherDinosaur)).isEqualTo(otherToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, opponentDinosaur)).isEqualTo(opponentPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, opponentDinosaur)).isEqualTo(opponentToughnessBefore);

        castAndResolveHandDinosaur();
        assertThat(gqs.getEffectivePower(gd, findPermanentForCard(handDinosaur))).isEqualTo(5);

        castAndResolveLibraryDinosaur();
        assertThat(gqs.getEffectivePower(gd, findPermanentForCard(libraryDinosaur))).isEqualTo(5);

        castAndResolveHandBear();
        assertThat(gqs.getEffectivePower(gd, findPermanentForCard(handBear))).isEqualTo(2);
    }

    @Test
    void attackTriggerDealsOneDamageToEachCreature() {
        Permanent source = addCreatureReady(player1, new StalwartSpeartail());
        Permanent otherCreature = addCreatureReady(player2, new GishathSunsAvatar());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isEqualTo(1);
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void attackDamagesBothPlayersPlaneswalkersWithoutDamagingPlayers() {
        addCreatureReady(player1, new StalwartSpeartail());
        harness.addToBattlefield(player1, new QuintoriusKand());
        harness.addToBattlefield(player2, new QuintoriusKand());
        Permanent ownPlaneswalker = findPermanent(player1, "Quintorius Kand");
        Permanent opposingPlaneswalker = findPermanent(player2, "Quintorius Kand");
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 4);
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void separateDamageEventsAccumulatePerpetualBoosts() {
        Permanent source = addCreatureReady(player1, new StalwartSpeartail());
        Permanent other = addCreatureReady(player1, new StalwartSpeartail());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();
        source.untap();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
    }

    @Test
    void enrageIncludesCreaturesThatGainDinosaurTypeOnlyOnBattlefield() {
        addCreatureReady(player1, new StalwartSpeartail());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Xenograft());
        findPermanent(player1, "Xenograft").setChosenSubtype(CardSubtype.DINOSAUR);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    private void castAndResolveHandDinosaur() {
        prepareMainPhase();
        addSpeartailMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castAndResolveLibraryDinosaur() {
        prepareMainPhase();
        addSpeartailMana();
        harness.castAndResolveFromLibraryTop(player1);
    }

    private void castAndResolveHandBear() {
        prepareMainPhase();
        Card bear = gd.playerHands.get(player1.getId()).getFirst();
        harness.castFromHand(player1, bear, "{1}{G}");
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addSpeartailMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent findPermanentForCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
