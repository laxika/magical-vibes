package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalambGardenSeeDAcademy.class, BalambGardenAirborne.class, Forest.class, GrizzlyBears.class})
class BalambGardenSeeDAcademyTest extends BaseCardTest {

    @Test
    void entersTappedAndAddsEitherGreenOrBlueMana() {
        harness.setHand(player1, List.of(new BalambGardenSeeDAcademy()));

        harness.playLand(player1, 0);
        Permanent garden = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(garden.isTapped()).isTrue();

        garden.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mana.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void transformsWithOneOtherTownReducingTheGenericCost() {
        Permanent garden = addReadyGarden();
        addTown();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garden.isTransformed()).isTrue();
        assertThat(garden.getCard()).isInstanceOf(BalambGardenAirborne.class);
    }

    @Test
    void crewingTheBackFaceLetsItAttackAndDraw() {
        Permanent garden = addTransformedGarden();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        Forest cardToDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(cardToDraw));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardToDraw);
    }

    @Test
    void canChooseGreenMana() {
        Permanent garden = addReadyGarden();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(garden.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void sourceAndOpposingTownsDoNotReduceTheCost() {
        Permanent garden = addReadyGarden();
        harness.addToBattlefield(player2, new BalambGardenSeeDAcademy());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(garden.isTapped()).isFalse();
        assertThat(garden.isTransformed()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(garden.isTapped()).isTrue();
        assertThat(garden.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(garden.isTransformed()).isTrue();
        assertThat(garden.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, garden)).isFalse();
    }

    @Test
    void extraTownsCannotReduceTheColoredManaRequirement() {
        Permanent garden = addReadyGarden();
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new BalambGardenSeeDAcademy());
        }
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(garden.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garden.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void summoningSickCreatureCanCrewTheBackFace() {
        Permanent garden = addTransformedGarden();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(garden.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, garden)).isTrue();
    }

    private Permanent addReadyGarden() {
        Permanent garden = harness.addToBattlefieldAndReturn(player1, new BalambGardenSeeDAcademy());
        garden.setSummoningSick(false);
        return garden;
    }

    private Permanent addTransformedGarden() {
        BalambGardenSeeDAcademy card = new BalambGardenSeeDAcademy();
        Permanent garden = harness.addToBattlefieldAndReturn(player1, card);
        garden.setSummoningSick(false);
        garden.setCard(card.getBackFaceCard());
        garden.setTransformed(true);
        return garden;
    }

    private void addTown() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new Forest());
        TestCards.mutableCard(town).setSubtypes(List.of(CardSubtype.TOWN));
    }
}
