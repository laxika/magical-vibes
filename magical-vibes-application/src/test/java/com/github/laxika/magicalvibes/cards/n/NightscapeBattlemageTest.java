package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightscapeBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without either kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player1, List.of(new NightscapeBattlemage()));
        addMana(2, ManaColor.BLACK);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nightscape Battlemage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Red kicker destroys a target land")
    void redKickerDestroysLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NightscapeBattlemage()));
        addMana(4, ManaColor.BLACK, ManaColor.RED);

        harness.castKickedCreature(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Blue kicker returns up to two target nonblack creatures")
    void blueKickerReturnsNonblackCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new NightscapeBattlemage()));
        addMana(4, ManaColor.BLACK, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId(), second.getId())
                .doesNotContain(blackCreature.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second, blackCreature);
    }

    @Test
    @DisplayName("Both kickers resolve independently")
    void bothKickers() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightscapeBattlemage()));
        addMana(6, ManaColor.BLACK, ManaColor.RED, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"), land.getId(), List.of(), true);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Blue kicker does not offer black creatures as targets")
    void blueKickerDoesNotOfferBlackCreatures() {
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new NightscapeBattlemage()));
        addMana(4, ManaColor.BLACK, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(nonblackCreature.getId())
                .doesNotContain(blackCreature.getId());
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments) {
        castWithAdditionalCosts(payments, null, List.of(), false);
    }

    private void castWithAdditionalCosts(List<String> payments, java.util.UUID targetId,
                                         List<java.util.UUID> targetIds, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, targetIds, List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
