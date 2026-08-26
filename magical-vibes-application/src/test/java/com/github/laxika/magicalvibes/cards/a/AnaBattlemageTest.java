package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaBattlemage.class, Forest.class, GrizzlyBears.class})
class AnaBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without either kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AnaBattlemage()));
        addMana(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ana Battlemage");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blue kicker makes a target player discard three cards")
    void blueKickerDiscardsThree() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new AnaBattlemage()));
        addMana(4, ManaColor.BLUE);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Black kicker taps an untapped creature and deals damage equal to its power")
    void blackKickerTapsCreatureAndDealsPowerDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AnaBattlemage()));
        addMana(3, ManaColor.BLACK);

        castWithAdditionalCosts(List.of("{1}{B}"));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Both kicker abilities resolve independently")
    void bothKickers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new AnaBattlemage()));
        addMana(5, ManaColor.BLUE, ManaColor.BLACK);

        castWithAdditionalCosts(List.of("{1}{B}"), player2.getId(), true);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Black kicker only permits untapped creatures as targets")
    void blackKickerOnlyTargetsUntappedCreatures() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new AnaBattlemage()));
        addMana(3, ManaColor.BLACK);

        castWithAdditionalCosts(List.of("{1}{B}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(untappedCreature.getId())
                .doesNotContain(tappedCreature.getId(), player1.getId());
    }

    private void addMana(int colorless, ManaColor... additionalColors) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, 1);
        for (ManaColor color : additionalColors) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments) {
        castWithAdditionalCosts(payments, null, false);
    }

    private void castWithAdditionalCosts(List<String> payments, UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
