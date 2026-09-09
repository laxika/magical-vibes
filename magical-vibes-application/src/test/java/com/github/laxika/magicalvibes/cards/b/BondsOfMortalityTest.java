package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
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

@CardUsed({BondsOfMortality.class, CarnageTyrant.class, DarksteelMyr.class})
class BondsOfMortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsCardOnEntry() {
        harness.setHand(player1, List.of(new BondsOfMortality()));
        harness.setLibrary(player1, List.of(new BondsOfMortality()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removes hexproof and indestructible from opponents' creatures until end of turn")
    void removesOpponentKeywordsUntilEndOfTurn() {
        Permanent ownHexproof = harness.addToBattlefieldAndReturn(player1, new CarnageTyrant());
        Permanent bonds = harness.addToBattlefieldAndReturn(player1, new BondsOfMortality());
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent opponentIndestructible = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.addMana(player1, ManaColor.GREEN, 1);

        int bondsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bonds);
        harness.activateAbility(player1, bondsIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
