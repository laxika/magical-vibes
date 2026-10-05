package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NicolBolasGodPharaoh.class, Forest.class, GrizzlyBears.class, Shock.class})
class NicolBolasGodPharaohTest extends BaseCardTest {

    // ===== +2: exile until nonland; may cast free this turn =====

    @Test
    @DisplayName("+2 exiles through lands until a nonland and grants free cast this turn")
    void plusTwoExilesUntilNonlandAndGrantsFreeCast() {
        Permanent bolas = addReadyBolas(player1, 5);
        Forest land1 = new Forest();
        Forest land2 = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, new ArrayList<>(List.of(land1, land2, bears)));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(bears.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());
        // Lands get no cast permission.
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land2.getId());
    }

    @Test
    @DisplayName("+2 free-cast permission expires at end of turn")
    void plusTwoPermissionExpiresAtEndOfTurn() {
        addReadyBolas(player1, 5);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, new ArrayList<>(List.of(bears)));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(bears.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("+2 cannot target self")
    void plusTwoCannotTargetSelf() {
        addReadyBolas(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== +1: each opponent exiles two from hand =====

    @Test
    @DisplayName("+1 prompts the opponent to exile two cards from hand")
    void plusOneExilesTwoFromOpponentHand() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new Shock())));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("+1 with fewer than two cards exiles the entire hand")
    void plusOneExilesWholeHandWhenFewerThanTwo() {
        addReadyBolas(player1, 5);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    // ===== −4: 7 damage to opponent / their creature / their planeswalker =====

    @Test
    @DisplayName("-4 deals 7 damage to target opponent")
    void minusFourDamagesOpponent() {
        Permanent bolas = addReadyBolas(player1, 5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("-4 deals 7 damage to a creature an opponent controls")
    void minusFourDamagesOpponentCreature() {
        addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 2, null, bearId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bearId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-4 cannot target own creature")
    void minusFourCannotTargetOwnCreature() {
        addReadyBolas(player1, 5);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-4 cannot target self as a player")
    void minusFourCannotTargetSelf() {
        addReadyBolas(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== −12: exile each nonland permanent opponents control =====

    @Test
    @DisplayName("-12 exiles opponents' nonland permanents and leaves lands")
    void minusTwelveExilesOpponentNonlands() {
        Permanent bolas = addReadyBolas(player1, 12);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears()); // own creature survives

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate -12 with insufficient loyalty")
    void cannotActivateMinusTwelveWithInsufficientLoyalty() {
        addReadyBolas(player1, 11);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    void plusTwoStopsAtFirstNonlandAndCastsItWithoutMana() {
        addReadyBolas(player1, 5);
        GrizzlyBears bears = new GrizzlyBears();
        Shock remaining = new Shock();
        harness.setLibrary(player2, List.of(new Forest(), bears, remaining));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bears);
    }

    @Test
    void plusTwoCannotCastCreatureOutsideMainPhase() {
        addReadyBolas(player1, 5);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
    }

    @Test
    void plusTwoPermissionSurvivesSourceLeavingBattlefield() {
        Permanent bolas = addReadyBolas(player1, 5);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(bolas);
        gd.playerGraveyards.get(player1.getId()).add(bolas.getCard());

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void plusTwoExilesAllLandLibraryWithoutGrantingPermission() {
        addReadyBolas(player1, 5);
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
    }

    @Test
    void plusTwoHandlesEmptyLibrary() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneSkipsEmptyHandAndLeavesControllersHandAlone() {
        addReadyBolas(player1, 5);
        Forest ownCard = new Forest();
        harness.setHand(player1, List.of(ownCard));
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void minusFourRemovesSevenLoyaltyFromOpposingPlaneswalker() {
        addReadyBolas(player1, 5);
        Permanent opponentBolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        opponentBolas.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, opponentBolas.getId());
        harness.passBothPriorities();

        assertThat(opponentBolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Nicol Bolas, God-Pharaoh");
    }

    @Test
    void minusFourStillDealsDamageWhenSourceDiesPayingLoyalty() {
        addReadyBolas(player1, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nicol Bolas, God-Pharaoh");
        harness.assertLife(player2, 13);
    }

    @Test
    void minusFourDoesNotDamageCreatureNowControlledByAbilityController() {
        addReadyBolas(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({InvasionOfZendikar.class})
    void minusFourCannotTargetOpponentsBattle() {
        addReadyBolas(player1, 5);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({Manalith.class})
    void minusFourCannotTargetNoncreatureArtifactOrLand() {
        addReadyBolas(player1, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Manalith());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({Manalith.class})
    void minusTwelveExilesArtifactsAndPlaneswalkersButPreservesOwnPermanents() {
        addReadyBolas(player1, 13);
        Manalith opposingArtifact = new Manalith();
        NicolBolasGodPharaoh opposingPlaneswalker = new NicolBolasGodPharaoh();
        harness.addToBattlefield(player2, opposingArtifact);
        Permanent opposingBolas = harness.addToBattlefieldAndReturn(player2, opposingPlaneswalker);
        opposingBolas.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player1, new Manalith());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(opposingArtifact, opposingPlaneswalker);
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Manalith");
        harness.assertOnBattlefield(player1, "Nicol Bolas, God-Pharaoh");
    }

    @Test
    @CardUsed({TormentingVoice.class})
    void plusTwoAllowsCastingCardWithMandatoryDiscardCost() {
        addReadyBolas(player1, 5);
        TormentingVoice voice = new TormentingVoice();
        harness.setLibrary(player2, List.of(voice));
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatCode(() -> harness.castFromExile(player1, voice.getId())).doesNotThrowAnyException();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private Permanent addReadyBolas(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NicolBolasGodPharaoh());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
