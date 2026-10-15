package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatedInfatuation.class, GrizzlyBears.class})
class FatedInfatuationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target creature you control")
    void createsTokenCopyOfTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Scries 2 when cast on your turn")
    void scriesOnYourTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    @DisplayName("Does not scry when cast on an opponent's turn")
    void doesNotScryOnOpponentsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast(player2, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatedInfatuation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal target prevents both copying and scrying")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new FatedInfatuation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Fated Infatuation");
    }

    @Test
    @DisplayName("Losing control of the target prevents copying and scrying")
    void doesNotResolveWhenTargetChangesController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new FatedInfatuation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        var target = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Fated Infatuation");
    }

    @Test
    @DisplayName("The token does not copy tapped status or marked damage")
    void doesNotCopyPermanentState() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var original = gd.playerBattlefields.get(player1.getId()).getFirst();
        original.tap();
        original.setMarkedDamage(1);

        cast(player2, original.getId());

        var token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getMarkedDamage()).isZero();
        assertThat(token.isSummoningSick()).isTrue();
        assertThat(original.isTapped()).isTrue();
        assertThat(original.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Scry uses the one remaining library card")
    void scriesWithOneCardInLibrary() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
    private void cast(com.github.laxika.magicalvibes.model.Player activePlayer, UUID targetId) {
        harness.setHand(player1, List.of(new FatedInfatuation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
