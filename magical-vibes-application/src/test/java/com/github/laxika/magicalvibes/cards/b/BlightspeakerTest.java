package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.r.RamosianCommander;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Blightspeaker.class, DefiantFalcon.class, GrizzlyBears.class, HolyDay.class,
        RamosianCommander.class})
class BlightspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability makes the targeted player lose 1 life")
    void targetPlayerLosesLife() {
        Permanent blightspeaker = addReadyBlightspeaker();
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(blightspeaker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability offers only Rebel permanents with mana value 3 or less")
    void searchesForEligibleRebelPermanent() {
        addReadyBlightspeaker();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new DefiantFalcon(),
                new RamosianCommander(),
                new GrizzlyBears(),
                new HolyDay()));

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Defiant Falcon");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Defiant Falcon");
    }

    @Test
    @DisplayName("The second ability does nothing when no eligible Rebel is in the library")
    void noEligibleRebelFound() {
        addReadyBlightspeaker();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new RamosianCommander(), new GrizzlyBears(), new HolyDay()));

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Ramosian Commander");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyBlightspeaker() {
        harness.addToBattlefield(player1, new Blightspeaker());
        Permanent blightspeaker = findPermanent(player1, "Blightspeaker");
        blightspeaker.setSummoningSick(false);
        return blightspeaker;
    }
}
