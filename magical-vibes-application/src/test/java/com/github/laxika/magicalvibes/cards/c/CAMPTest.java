package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.e.ExoticOrchard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CAMP.class, EnsoulArtifact.class, ExoticOrchard.class, Forest.class, GrizzlyBears.class, Mountain.class})
class CAMPTest extends BaseCardTest {

    @Test
    @DisplayName("A matching mana color puts a counter on a chosen creature and creates a Junk")
    void matchingManaColorCountersCreatureAndCreatesJunk() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 1);
        chooseTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonmatching mana color still puts the counter on the creature")
    void nonmatchingManaColorDoesNotCreateJunk() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        camp.setAttachedTo(mountain.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 1);
        chooseTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    @DisplayName("The color chosen for an any-color land is used by the trigger")
    void chosenAnyColorManaIsUsedByTrigger() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent orchard = harness.addToBattlefieldAndReturn(player1, new ExoticOrchard());
        camp.setAttachedTo(orchard.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");
        chooseTarget(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }


    @Test
    void fortifyAttachesAndMovesBetweenControlledLands() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        assertThat(camp.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(camp.getAttachedTo()).isEqualTo(forest.getId());

        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();
        assertThat(camp.getAttachedTo()).isEqualTo(mountain.getId());
    }

    @Test
    void fortifyRejectsOpponentsLand() {
        harness.addToBattlefield(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fortifyRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyFortifiedLandTriggers() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent fortified = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(fortified.getId());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void junkExilesTopCardAndAllowsNormalCostCasting() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.tapPermanent(player1, 1);
        chooseTarget(bears);
        Permanent junk = findPermanent(player1, "Junk");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk),
                0, null, null);
        assertThat(findPermanents(player1, "Junk")).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    void junkRequiresSorceryTiming() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.tapPermanent(player1, 1);
        chooseTarget(bears);
        Permanent junk = findPermanent(player1, "Junk");
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(junk), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Junk")).containsExactly(junk);
    }

    @Test
    void animatedCampBecomesUnattached() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, camp.getId());
        harness.passBothPriorities();

        assertThat(camp.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "C.A.M.P.");
    }


    @Test
    void removedCreatureTargetDoesNotCreateJunk() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.tapPermanent(player1, 1);
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void triggerCannotTargetOpponentsCreature() {
        Permanent camp = harness.addToBattlefieldAndReturn(player1, new CAMP());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        camp.setAttachedTo(forest.getId());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.tapPermanent(player1, 1);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        chooseTarget(ownBears);

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    private void chooseTarget(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
