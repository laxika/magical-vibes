package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SabinMasterMonk.class, GrizzlyBears.class})
class SabinMasterMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not use blitz")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new SabinMasterMonk()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sabin);
    }

    @Test
    @DisplayName("Blitz from hand grants haste, draws on death, and sacrifices at the next end step")
    void blitzFromHandGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new SabinMasterMonk(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addBlitzMana();

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sabin, Master Monk");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blitz can be cast from the graveyard by discarding a card")
    void blitzFromGraveyardGrantsHasteDrawsAndSacrifices() {
        harness.setGraveyard(player1, List.of(new SabinMasterMonk()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addBlitzMana();

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sabin = findPermanent(player1, "Sabin, Master Monk");
        assertThat(gqs.hasKeyword(gd, sabin, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sabin, Master Monk");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addBlitzMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
