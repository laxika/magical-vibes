package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
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

@CardUsed({GirderGoons.class, GrizzlyBears.class, NantukoHusk.class})
class GirderGoonsTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast creates a tapped Rogue on death without drawing")
    void normalCastCreatesTokenWithoutBlitzDraw() {
        harness.addToBattlefield(player1, new NantukoHusk());
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent goons = findPermanent(player1, "Girder Goons");
        assertThat(gqs.hasKeyword(gd, goons, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, goons.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Girder Goons");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent rogue = findPermanents(player1, "Rogue").getFirst();
        assertThat(rogue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new GirderGoons()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent goons = findPermanent(player1, "Girder Goons");
        assertThat(gqs.hasKeyword(gd, goons, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goons);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Girder Goons");
        harness.assertInHand(player1, "Grizzly Bears");
        Permanent rogue = findPermanents(player1, "Rogue").getFirst();
        assertThat(rogue.isTapped()).isTrue();
    }
}
