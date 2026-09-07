package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.t.TimeElemental;
import com.github.laxika.magicalvibes.cards.t.TheHive;
import com.github.laxika.magicalvibes.cards.w.WoundReflection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BronzeTablet.class, ControlMagic.class, GrizzlyBears.class, Shatter.class, TimeElemental.class,
        TheHive.class, WoundReflection.class})
class BronzeTabletTest extends BaseCardTest {

    private boolean inExile(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    void entersBattlefieldTapped() {
        Permanent tablet = harness.enterBattlefieldAndReturn(player1, new BronzeTablet());
        assertThat(tablet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying 10 life puts Bronze Tablet into its owner's graveyard; the target stays exiled")
    void payingKeepsOwnership() {
        Permanent tablet = addReadyTablet();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());
        harness.passBothPriorities(); // resolve ability -> may-pay prompt for player2

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        // Bronze Tablet leaves exile for its owner's graveyard; the target permanent stays exiled.
        harness.assertInGraveyard(player1, "Bronze Tablet");
        assertThat(inExile("Bronze Tablet")).isFalse();
        assertThat(inExile("Grizzly Bears")).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to pay leaves both cards exiled (ownership swap not modeled)")
    void decliningExiles() {
        Permanent tablet = addReadyTablet();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Both remain exiled; Bronze Tablet is not put into a graveyard.
        assertThat(inExile("Bronze Tablet")).isTrue();
        assertThat(inExile("Grizzly Bears")).isTrue();
        harness.assertNotInGraveyard(player1, "Bronze Tablet");
    }

    @Test
    @DisplayName("An owner who can't pay 10 life resolves the ante swap automatically with no prompt")
    void cannotPayResolvesAutomatically() {
        Permanent tablet = addReadyTablet();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player2, 5);

        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());
        harness.passBothPriorities();

        // No pay prompt is raised — the ante swap resolves on its own.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
        assertThat(inExile("Bronze Tablet")).isTrue();
        assertThat(inExile("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("The ability can't target a permanent the activating player owns")
    void cannotTargetOwnPermanent() {
        Permanent tablet = addReadyTablet();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, tabletIndex(tablet), null, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nontoken permanent an opponent owns");
    }

    @Test
    void sourceLeavingBeforeResolutionStillOffersPayment() {
        Permanent tablet = addReadyTablet();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());

        harness.setHand(player2, java.util.List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, tablet.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tablet.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tablet);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tablet.getOriginalCard());
        assertThat(gd.exiledCards.stream().anyMatch(entry -> entry.card() == tablet.getOriginalCard())).isFalse();
        assertThat(gd.exiledCards.stream().anyMatch(entry -> entry.card() == bears.getOriginalCard())).isTrue();
    }

    @Test
    void sourceReturnedToHandBeforeResolutionIsNotMovedOnPayment() {
        Permanent tablet = addReadyTablet();
        addCreatureReady(player2, new TimeElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());

        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.activateAbility(player2, 0, null, tablet.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(tablet.getOriginalCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(tablet.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(tablet.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void targetLeavingBeforeResolutionFizzles() {
        Permanent tablet = addReadyTablet();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tablet);
        assertThat(tablet.isTapped()).isTrue();
        assertThat(gd.exiledCards.stream().anyMatch(entry -> entry.card() == tablet.getOriginalCard())).isFalse();
        assertThat(gd.exiledCards.stream().anyMatch(entry -> entry.card() == bears.getOriginalCard())).isFalse();
    }

    @Test
    void targetsOpponentOwnedPermanentUnderYourControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent tablet = addReadyTablet();
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tablet.getOriginalCard());
        assertThat(gd.exiledCards.stream().anyMatch(entry -> entry.card() == bears.getOriginalCard())).isTrue();
    }

    @Test
    void cannotTargetToken() {
        harness.addToBattlefield(player2, new TheHive());
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent tablet = addReadyTablet();
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, tabletIndex(tablet), null, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void payingLifeIsCountedAsLifeLost() {
        harness.addToBattlefield(player1, new WoundReflection());
        Permanent tablet = addReadyTablet();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, tabletIndex(tablet), null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(0);
    }

    private Permanent addReadyTablet() {
        Permanent tablet = harness.enterBattlefieldAndReturn(player1, new BronzeTablet());
        tablet.untap();
        return tablet;
    }

    private int tabletIndex(Permanent tablet) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(tablet);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
