package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Conspiracy.class, CateranBrute.class, Forest.class})
class ConspiracyTest extends BaseCardTest {

    @Test
    void choosesCreatureTypeAndGrantsItToControlledCreatures() {
        harness.addToBattlefield(player1, new CateranBrute());
        harness.addToBattlefield(player2, new CateranBrute());

        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        Permanent controlledCreature = findPermanent(player1, "Cateran Brute");
        assertThat(gqs.computeStaticBonus(gd, controlledCreature).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, findPermanent(player2, "Cateran Brute")).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    void grantsChosenTypeToOwnedCreatureCardsOutsideTheBattlefield() {
        CateranBrute handCreature = new CateranBrute();
        CateranBrute graveyardCreature = new CateranBrute();
        CateranBrute libraryCreature = new CateranBrute();
        CateranBrute exiledCreature = new CateranBrute();
        CateranBrute opponentCreature = new CateranBrute();
        harness.setHand(player1, List.of(handCreature));
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.setExile(player1, List.of(exiledCreature));
        harness.setHand(player2, List.of(opponentCreature));

        Permanent conspiracyPermanent = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracyPermanent.setChosenSubtype(CardSubtype.GOBLIN);

        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(libraryCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(exiledCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(opponentCreature, CardSubtype.GOBLIN, gd, player2.getId())).isFalse();
    }

    @Test
    void grantsChosenTypeToCreatureSpellsYouControl() {
        Permanent conspiracyPermanent = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracyPermanent.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new CateranBrute(), "{2}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.cardHasSubtype(gd.stack.getFirst().getCard(), CardSubtype.GOBLIN, gd, player1.getId()))
                .isTrue();
    }

    @Test
    void doesNotGrantChosenTypeToNoncreatures() {
        Permanent conspiracyPermanent = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracyPermanent.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent forestPermanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest forestInHand = new Forest();
        harness.setHand(player1, List.of(forestInHand));

        assertThat(gqs.computeStaticBonus(gd, forestPermanent).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);
        assertThat(gqs.cardHasSubtype(forestInHand, CardSubtype.GOBLIN, gd, player1.getId()))
                .isFalse();
    }

    @Test
    void doesNotGrantChosenTypeBeforeCreatureTypeIsChosen() {
        harness.addToBattlefield(player1, new CateranBrute());
        harness.addToBattlefield(player1, new Conspiracy());

        assertThat(gqs.computeStaticBonus(gd, findPermanent(player1, "Cateran Brute")).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);
    }
}
