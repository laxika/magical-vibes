package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AethersphereHarvester;
import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartOfKiran;
import com.github.laxika.magicalvibes.cards.h.HighSpeedHoverbike;
import com.github.laxika.magicalvibes.cards.h.Hulldrifter;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
import com.github.laxika.magicalvibes.cards.y.SupportSkyforge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        SupportSkyforge.class,
        HeartOfKiran.class,
        HighSpeedHoverbike.class,
        SkySkiff.class,
        SmugglersCopter.class,
        AethersphereHarvester.class,
        AirResponseUnit.class,
        Hulldrifter.class,
        SkysovereignConsulFlagship.class,
        GrizzlyBears.class
})
class SupportSkyforgeTest extends BaseCardTest {

    @Test
    void newlyCreatedServosCanCrewAndAnimationExpiresAtEndOfTurn() {
        Permanent skyforge = harness.enterBattlefieldAndReturn(player1, new SupportSkyforge());
        harness.passBothPriorities();
        List<Permanent> servos = findPermanents(player1, "Servo");

        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.isSummoningSick()).isTrue();
            assertThat(gqs.getEffectiveColors(gd, servo)).isEmpty();
        });
        assertThat(gqs.isCreature(gd, skyforge)).isFalse();
        harness.activateAbility(player1, 0, null, null);

        assertThat(servos).hasSize(4).allSatisfy(servo -> assertThat(servo.isTapped()).isTrue());
        assertThat(gqs.isCreature(gd, skyforge)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, skyforge)).isTrue();
        assertThat(gqs.isArtifact(gd, skyforge)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, skyforge)).isFalse();
        assertThat(gqs.isArtifact(gd, skyforge)).isTrue();
    }

    @Test
    void threeUntappedServosAreNotEnoughToCrew() {
        Permanent skyforge = harness.enterBattlefieldAndReturn(player1, new SupportSkyforge());
        harness.passBothPriorities();
        List<Permanent> servos = findPermanents(player1, "Servo");
        servos.getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(gqs.isCreature(gd, skyforge)).isFalse();
        assertThat(servos.stream().filter(Permanent::isTapped).count()).isEqualTo(1);
    }

    @Test
    void draftOffersDistinctSpellbookCardsAndCreatureChangeSurvivesEnteringBattlefieldAndCleanup() {
        Permanent skyforge = harness.enterBattlefieldAndReturn(player1, new SupportSkyforge());
        harness.passBothPriorities();
        skyforge.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        assertThat(skyforge.isTapped()).isFalse();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
        assertThat(choice.allCards().stream().map(Card::getName).toList())
                .doesNotHaveDuplicates()
                .isSubsetOf("Heart of Kiran", "High-Speed Hoverbike", "Sky Skiff", "Smuggler's Copter",
                        "Aethersphere Harvester", "Air Response Unit", "Hulldrifter",
                        "Skysovereign, Consul Flagship");
        Card selected = choice.allCards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        gd.playerHands.get(player1.getId()).remove(selected);
        Permanent draftedVehicle = harness.addToBattlefieldAndReturn(player1, selected);
        assertThat(gqs.isCreature(gd, draftedVehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, draftedVehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, draftedVehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, draftedVehicle)).isTrue();
        assertThat(gqs.isCreature(gd, skyforge)).isFalse();
    }

    @Test
    @DisplayName("Enters with four 1/1 colorless Servo artifact creature tokens")
    void createsFourServosOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new SupportSkyforge());
        harness.passBothPriorities();

        List<Permanent> servos = findPermanents(player1, "Servo");
        assertThat(servos).hasSize(4);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().getPower()).isEqualTo(1);
            assertThat(servo.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Attacking drafts three spellbook cards and makes the selected card an artifact creature")
    void attacksDraftAndPerpetuallyChangesSelectedCard() {
        addCreatureReady(player1, new SupportSkyforge());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);

        Card selected = choice.allCards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(selected);
        assertThat(selected.hasType(CardType.ARTIFACT)).isTrue();
        assertThat(selected.hasType(CardType.CREATURE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
