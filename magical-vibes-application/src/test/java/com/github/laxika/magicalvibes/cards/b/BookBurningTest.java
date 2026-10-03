package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.p.PrismaticStrands;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BookBurning.class, BorderPatrol.class, SuntailHawk.class, PrismaticStrands.class})
class BookBurningTest extends BaseCardTest {

    @Test
    @DisplayName("A player accepting takes 6 damage and prevents the mill")
    void acceptingDamagePreventsMill() {
        harness.setLibrary(player2, List.of(new BorderPatrol(), new BorderPatrol(), new BorderPatrol(),
                new BorderPatrol(), new BorderPatrol(), new BorderPatrol()));
        int lifeBefore = gd.getLife(player1.getId());
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("All players declining mills six cards from the target player")
    void allPlayersDecliningMillsTarget() {
        harness.setLibrary(player2, List.of(new BorderPatrol(), new BorderPatrol(), new BorderPatrol(),
                new BorderPatrol(), new BorderPatrol(), new BorderPatrol()));
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The first accepting player stops the remaining choices")
    void firstAcceptanceStopsChoices() {
        harness.setLibrary(player2, List.of(new BorderPatrol(), new BorderPatrol(), new BorderPatrol(),
                new BorderPatrol(), new BorderPatrol(), new BorderPatrol()));
        int lifeBefore = gd.getLife(player2.getId());
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Requires a player target")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorderPatrol());
        harness.setHand(player1, List.of(new BookBurning()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    private void castBookBurning(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new BookBurning()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("A player accepting takes 6 damage and prevents the mill")
    void acceptingDamagePreventsMillJudReview() {
        harness.setLibrary(player2, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk(),
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));
        int lifeBefore = gd.getLife(player1.getId());
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The first accepting player stops the remaining choices")
    void firstAcceptanceStopsChoicesJudReview() {
        harness.setLibrary(player2, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk(),
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));
        int lifeBefore = gd.getLife(player2.getId());
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("All players declining mills only the cards available in a short library")
    void allPlayersDecliningMillsAvailableCards() {
        harness.setLibrary(player2, List.of(new SuntailHawk(), new SuntailHawk()));
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Accepting prevented damage still prevents milling")
    void preventedDamageStillPreventsMill() {
        harness.castFromHand(player1, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.setLibrary(player2, List.of(new SuntailHawk(), new SuntailHawk()));
        int lifeBefore = gd.getLife(player2.getId());
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster may target themselves and mills exactly the top six cards")
    void selfTargetMillsTopSixCards() {
        List<SuntailHawk> milled = List.of(new SuntailHawk(), new SuntailHawk(),
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk(), new SuntailHawk());
        BorderPatrol remaining = new BorderPatrol();
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2),
                milled.get(3), milled.get(4), milled.get(5), remaining));
        int lifeBefore = gd.getLife(player1.getId());
        castBookBurning(player1.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(milled);
        harness.assertInGraveyard(player1, "Book Burning");
    }

    @Test
    @DisplayName("An empty target library does not skip the damage choices")
    void emptyLibraryStillAllowsDamageChoice() {
        harness.setLibrary(player2, List.of());
        int lifeBefore = gd.getLife(player1.getId());
        castBookBurning(player2.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 6);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
