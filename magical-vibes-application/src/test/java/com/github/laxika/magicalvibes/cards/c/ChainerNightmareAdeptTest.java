package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainerNightmareAdept.class, Forest.class, GrizzlyBears.class})
class ChainerNightmareAdeptTest extends BaseCardTest {

    @Test
    void discardingAllowsOneCreatureCastFromGraveyard() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bearPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bearPermanent, Keyword.HASTE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonHandCreatureEntersWithHasteUntilYourNextTurn() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());

        Permanent bear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureCastFromHandDoesNotGainHaste() {
        harness.addToBattlefield(player1, new ChainerNightmareAdept());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }
}
