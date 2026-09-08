package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChartoothCougar.class, Mountain.class, Forest.class})
class ChartoothCougarTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives Chartooth Cougar +1/+0 until end of turn")
    void redAbilityBoostsPower() {
        Permanent cougar = addCreatureReady(player1, new ChartoothCougar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cougar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cougar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mountaincycling discards the card and searches for a Mountain")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new ChartoothCougar()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chartooth Cougar");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().getFirst()).isInstanceOf(Mountain.class);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Mountain");
    }
}
