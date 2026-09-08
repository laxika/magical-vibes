package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NightClubber.class, GrizzlyBears.class})
class NightClubberTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives only opponents' creatures -1/-1 until end of turn")
    void etbDebuffsOpponentsCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Normal cast does not grant blitz haste or delayed sacrifice")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clubber = findPermanent(player1, "Night Clubber");
        assertThat(gqs.hasKeyword(gd, clubber, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clubber);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new NightClubber()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clubber = findPermanent(player1, "Night Clubber");
        assertThat(gqs.hasKeyword(gd, clubber, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clubber);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Night Clubber");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
