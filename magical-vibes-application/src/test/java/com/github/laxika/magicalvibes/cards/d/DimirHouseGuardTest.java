package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimirHouseGuard.class, HillGiant.class, GrizzlyBears.class})
class DimirHouseGuardTest extends BaseCardTest {

    @Test
    void transmuteSearchesForTheSameManaValue() {
        DimirHouseGuard houseGuard = new DimirHouseGuard();
        HillGiant matchingCard = new HillGiant();
        GrizzlyBears differentManaValue = new GrizzlyBears();
        harness.setHand(player1, List.of(houseGuard));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Dimir House Guard");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void sacrificingACreatureRegeneratesDimirHouseGuard() {
        Permanent houseGuard = new Permanent(new DimirHouseGuard());
        gd.playerBattlefields.get(player1.getId()).add(houseGuard);
        Permanent fodder = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(fodder);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(houseGuard.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
