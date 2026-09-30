package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GishathSunsAvatar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalwartSpeartail.class, FutureSight.class, GishathSunsAvatar.class, GrizzlyBears.class})
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
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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
