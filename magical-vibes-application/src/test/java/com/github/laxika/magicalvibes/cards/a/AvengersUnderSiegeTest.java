package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BaronStruckerHYDRAOverlord;
import com.github.laxika.magicalvibes.cards.d.DocOckSinisterScientist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvengersUnderSiege.class, BaronStruckerHYDRAOverlord.class,
        DocOckSinisterScientist.class, GrizzlyBears.class})
class AvengersUnderSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates two 2/1 black Villain tokens with menace")
    void chapterICreatesVillains() {
        harness.setHand(player1, List.of(new AvengersUnderSiege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VILLAIN);
            assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
        });
    }

    @Test
    @DisplayName("Chapter II damages non-Villain creatures and each opponent")
    void chapterIIDamagesNonVillainsAndOpponents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent villain = harness.addToBattlefieldAndReturn(player2, new DocOckSinisterScientist());
        harness.addToBattlefield(player1, new AvengersUnderSiege());
        Permanent saga = findPermanent(player1, "Avengers: Under Siege");
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(villain);
    }

    @Test
    @DisplayName("Chapter III creates a Treasure for each Villain controlled")
    void chapterIIICreatesTreasureForEachVillain() {
        harness.addToBattlefield(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player1, new BaronStruckerHYDRAOverlord());
        harness.addToBattlefield(player1, new AvengersUnderSiege());
        Permanent saga = findPermanent(player1, "Avengers: Under Siege");
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Avengers: Under Siege")).isEmpty();
    }
}
