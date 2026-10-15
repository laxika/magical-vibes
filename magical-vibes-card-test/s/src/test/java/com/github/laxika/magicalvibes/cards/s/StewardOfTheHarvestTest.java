package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StewardOfTheHarvest.class, Forest.class, GrizzlyBears.class, RodOfRuin.class,
        TerramorphicExpanse.class})
class StewardOfTheHarvestTest extends BaseCardTest {

    @Test
    void exilesUpToThreeLandCardsOnlyFromControllersGraveyard() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card nonland = new RodOfRuin();
        Card opponentLand = new Forest();
        harness.setGraveyard(player1, List.of(first, second, third, fourth, nonland));
        harness.setGraveyard(player2, List.of(opponentLand));

        Permanent steward = castSteward();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.cards()).containsExactly(first, second, third, fourth);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(steward.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fourth, nonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void creaturesYouControlGainExiledLandAbilitiesIncludingBasicLandMana() {
        Permanent steward = addCreatureReady(player1, new StewardOfTheHarvest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card forest = new Forest();
        gd.addToExile(player1.getId(), forest, steward.getId());

        assertThat(gqs.computeStaticBonus(gd, steward).grantedActivatedAbilities()).hasSize(1);
        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).hasSize(1);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void noncreaturesAndOpponentsCreaturesDoNotGainTheAbilities() {
        Permanent steward = addCreatureReady(player1, new StewardOfTheHarvest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.addToExile(player1.getId(), new Forest(), steward.getId());

        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).hasSize(1);
        assertThat(gqs.computeStaticBonus(gd, rod).grantedActivatedAbilities()).isEmpty();
        assertThat(gqs.computeStaticBonus(gd, opponentBears).grantedActivatedAbilities()).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseZeroTargetsWithLandCardsAvailable() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        Permanent steward = castSteward();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.getCardsExiledByPermanent(steward.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithNoEligibleCardsInOwnGraveyard() {
        Card nonland = new RodOfRuin();
        Card opponentLand = new Forest();
        harness.setGraveyard(player1, List.of(nonland));
        harness.setGraveyard(player2, List.of(opponentLand));

        Permanent steward = castSteward();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
        assertThat(gd.getCardsExiledByPermanent(steward.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureEnteringAfterExileCanUseTheGrantedManaAbility() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        castSteward();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.playerManaPools.get(player1.getId()).clear();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void grantedTapAbilityStillRequiresCreatureToBeFreeOfSummoningSickness() {
        Permanent steward = addCreatureReady(player1, new StewardOfTheHarvest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);
        gd.addToExile(player1.getId(), new Forest(), steward.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void inheritedSacrificeAbilitySacrificesTheCreatureAndFindsATappedBasicLand() {
        Permanent steward = addCreatureReady(player1, new StewardOfTheHarvest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card expanse = new TerramorphicExpanse();
        Card forest = new Forest();
        gd.addToExile(player1.getId(), expanse, steward.getId());
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(steward);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        assertThat(gd.getCardsExiledByPermanent(steward.getId())).containsExactly(expanse);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent foundLand = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(foundLand.getCard()).isSameAs(forest);
        assertThat(foundLand.isTapped()).isTrue();
    }

    @Test
    void sacrificingStewardRemovesTheGrantButItsActivatedAbilityStillResolves() {
        Permanent steward = addCreatureReady(player1, new StewardOfTheHarvest());
        addCreatureReady(player1, new GrizzlyBears());
        Card expanse = new TerramorphicExpanse();
        Card forest = new Forest();
        gd.addToExile(player1.getId(), expanse, steward.getId());
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(steward.getCard());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent foundLand = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(foundLand.getCard()).isSameAs(forest);
        assertThat(foundLand.isTapped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(steward.getId())).containsExactly(expanse);
    }

    @Test
    void landsExiledWithoutThisStewardDoNotGrantAbilities() {
        addCreatureReady(player1, new StewardOfTheHarvest());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSteward = harness.addToBattlefieldAndReturn(player2, new StewardOfTheHarvest());
        gd.addToExile(player1.getId(), new Forest());
        gd.addToExile(player2.getId(), new Forest(), opponentSteward.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent castSteward() {
        harness.setHand(player1, List.of(new StewardOfTheHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

}
