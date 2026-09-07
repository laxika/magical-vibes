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

    private Permanent addReadyGarden() {
        Permanent garden = harness.addToBattlefieldAndReturn(player1, new BalambGardenSeeDAcademy());
        garden.setSummoningSick(false);
        return garden;
    }

    private Permanent addTransformedGarden() {
        BalambGardenSeeDAcademy card = new BalambGardenSeeDAcademy();
        Permanent garden = new Permanent(card);
        garden.setSummoningSick(false);
        garden.setCard(card.getBackFaceCard());
        garden.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(garden);
        return garden;
    }

    private void addTown() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new Forest());
        TestCards.mutableCard(town).setSubtypes(List.of(CardSubtype.TOWN));
    }
}
