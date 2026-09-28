package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MezzioMugger.class, Forest.class, GiantGrowth.class, GrizzlyBears.class})
class MezzioMuggerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles the top card of each library and grants play permission")
    void attackingExilesTopCardOfEachLibrary() {
        Card ownTopCard = new Forest();
        Card opposingTopCard = new GiantGrowth();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        addCreatureReady(player1, new MezzioMugger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownTopCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingTopCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(ownTopCard.getId(), player1.getId())
                .containsEntry(opposingTopCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(ownTopCard.getId(), opposingTopCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(opposingTopCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(ownTopCard.getId());
    }

    @Test
    @DisplayName("An exiled spell can be cast with mana of any color")
    void castsExiledSpellWithAnyColorMana() {
        Card ownTopCard = new Forest();
        GiantGrowth opposingTopCard = new GiantGrowth();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        addCreatureReady(player1, new MezzioMugger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, opposingTopCard.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownTopCard);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new MezzioMugger()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mugger = findPermanent(player1, "Mezzio Mugger");
        assertThat(gqs.hasKeyword(gd, mugger, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mezzio Mugger");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
