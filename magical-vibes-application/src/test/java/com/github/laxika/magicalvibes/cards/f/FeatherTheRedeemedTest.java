package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BandTogether;
import com.github.laxika.magicalvibes.cards.c.CallousDismissal;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeatherTheRedeemed.class, GiantGrowth.class, GrizzlyBears.class,
        BandTogether.class, CallousDismissal.class})
class FeatherTheRedeemedTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a targeted instant and returns it at the next end step")
    void exilesAndReturnsTargetedSpell() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, java.util.List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(growth);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(growth);
        assertThat(gd.playerHands.get(player1.getId())).contains(growth);
    }

    @Test
    void doesNotExileSpellOwnedByAnotherPlayer() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GiantGrowth growth = new GiantGrowth();
        growth.setOwnerId(player2.getId());
        gd.addToExile(player2.getId(), growth);
        gd.exilePlayPermissions.put(growth.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, growth.getId(), bears.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(growth);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(growth);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(growth);
    }

    @Test
    @DisplayName("Does not trigger when the spell targets an opponent's creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, opponentBears.getId());

        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getName().equals("Giant Growth"));
    }

    @Test
    @DisplayName("Does not exile a spell that fizzles")
    void doesNotExileFizzledSpell() {
        harness.addToBattlefield(player1, new FeatherTheRedeemed());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getName().equals("Giant Growth"));
    }

    @Test
    void delayedReturnHasFeatherAsItsSource() {
        FeatherTheRedeemed feather = new FeatherTheRedeemed();
        Permanent source = harness.addToBattlefieldAndReturn(player1, feather);
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, java.util.List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, source.getId());
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(feather);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(growth);
    }

    @Test
    void sorceryStillReturnsWhenItBouncesFeather() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new FeatherTheRedeemed());
        CallousDismissal dismissal = new CallousDismissal();
        harness.setHand(player1, java.util.List.of(dismissal));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, feather.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Feather, the Redeemed");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dismissal);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(dismissal);
    }

    @Test
    void triggersOnceForSpellWithFriendlyAndOpposingTargets() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new FeatherTheRedeemed());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BandTogether band = new BandTogether();
        harness.setHand(player1, java.util.List.of(band));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, java.util.List.of(opponent.getId(), feather.getId()));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(band);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(band);
    }

    @Test
    void doesNotTriggerForOpponentsSpellTargetingYourCreature() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new FeatherTheRedeemed());
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player2, java.util.List.of(growth));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, feather.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(growth);
    }

    @Test
    void spellCastDuringEndStepWaitsForFollowingEndStep() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new FeatherTheRedeemed());
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, java.util.List.of(growth));
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, feather.getId());
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(growth);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(growth);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(growth);
    }
}
