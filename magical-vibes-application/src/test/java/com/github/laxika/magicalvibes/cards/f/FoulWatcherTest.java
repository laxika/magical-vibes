package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoulWatcher.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class})
class FoulWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, surveils one")
    void entersWithSurveilOne() {
        GameData gd = harness.getGameData();
        Card topCard = new GrizzlyBears();
        Card remainingCard = new Millstone();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.setHand(player1, List.of(new FoulWatcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Gets +1/+0 with delirium")
    void getsDeliriumBonus() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new FoulWatcher());

        Permanent watcher = findPermanent(player1, "Foul Watcher");
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }
}
