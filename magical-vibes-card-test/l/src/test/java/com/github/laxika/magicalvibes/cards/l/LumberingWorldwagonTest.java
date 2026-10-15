package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KeenBuccaneer;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumberingWorldwagon.class, Forest.class, Mountain.class, Island.class, KeenBuccaneer.class})
class LumberingWorldwagonTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of lands you control and toughness stays 4")
    void powerCountsControlledLands() {
        Permanent wagon = addReadyWagon();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, wagon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wagon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering the battlefield may search for a basic land and puts it tapped")
    void entersWithBasicLandSearch() {
        setupLibrary();
        harness.setHand(player1, List.of(new LumberingWorldwagon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        searchAndChooseLand();
    }

    @Test
    @DisplayName("Attacking may search for a basic land after the Vehicle is crewed")
    void attackingWithWagonTriggersSearch() {
        setupLibrary();
        Permanent wagon = addReadyWagon();
        addCreatureReady(player1, new KeenBuccaneer());
        addCreatureReady(player1, new KeenBuccaneer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(wagon.isTapped()).isTrue();
    }

    @Test
    void powerUpdatesWhenControlledLandsEnterAndLeave() {
        Permanent wagon = addReadyWagon();
        assertThat(gqs.getEffectivePower(gd, wagon)).isZero();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, wagon)).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        assertThat(gqs.getEffectivePower(gd, wagon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wagon)).isEqualTo(4);
    }

    @Test
    void attackSearchAddsTappedLandAndIncreasesPower() {
        setupLibrary();
        Permanent wagon = addReadyWagon();
        harness.addToBattlefield(player1, new KeenBuccaneer());
        harness.addToBattlefield(player1, new KeenBuccaneer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        searchAndChooseLand();

        assertThat(gqs.getEffectivePower(gd, wagon)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void crewCanTapSummoningSickCreaturesAndDoesNotTapVehicle() {
        Permanent wagon = addReadyWagon();
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        firstCrew.setSummoningSick(true);
        secondCrew.setSummoningSick(true);

        assertThat(gqs.isCreature(gd, wagon)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(wagon.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, wagon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wagon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wagon)).isEqualTo(4);
    }

    @Test
    void insufficientCrewPowerCannotActivate() {
        Permanent wagon = addReadyWagon();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, wagon)).isFalse();
    }

    @Test
    void enteringSearchCanBeDeclined() {
        setupLibrary();
        harness.setHand(player1, List.of(new LumberingWorldwagon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void acceptedSearchWithNoBasicLandsFinishesWithoutAddingLand() {
        harness.setLibrary(player1, List.of(new KeenBuccaneer()));
        harness.setHand(player1, List.of(new LumberingWorldwagon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyWagon() {
        return addCreatureReady(player1, new LumberingWorldwagon());
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Island(), new KeenBuccaneer()));
    }

    private void searchAndChooseLand() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, chosenName);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
