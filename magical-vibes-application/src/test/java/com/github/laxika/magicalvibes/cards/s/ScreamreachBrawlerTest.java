package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(ScreamreachBrawler.class)
class ScreamreachBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent brawler = findPermanent(player1, "Screamreach Brawler");
        assertThat(brawler.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanent(player1, "Screamreach Brawler")).isSameAs(brawler);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent brawler = findPermanent(player1, "Screamreach Brawler");
        assertThat(brawler.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Screamreach Brawler");
        harness.assertNotInHand(player1, "Screamreach Brawler");
        assertThat(gd.stack).hasSize(1);
        harness.passUntil(TurnStep.CLEANUP);

        harness.assertInHand(player1, "Screamreach Brawler");
        harness.assertNotOnBattlefield(player1, "Screamreach Brawler");
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield triggered ability")
    void dashDoesNotCreateAnEnterBattlefieldTrigger() {
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Screamreach Brawler");
        assertThat(gd.stack).isEmpty();
    }
}
