package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrototypePortalTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB imprint MayEffect and activated ability with CreateTokenCopyOfImprintedCardEffect(false, false)")
    void hasCorrectStructure() {
        PrototypePortal card = new PrototypePortal();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ExileFromHandToImprintEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{X}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(1)
                .first()
                .isInstanceOf(CreateTokenCopyOfImprintedCardEffect.class);

        CreateTokenCopyOfImprintedCardEffect effect = (CreateTokenCopyOfImprintedCardEffect) card.getActivatedAbilities().getFirst().getEffects().getFirst();
        assertThat(effect.grantHaste()).isFalse();
        assertThat(effect.exileAtEndStep()).isFalse();
    }

    // ===== ETB imprint =====

    @Test
    @DisplayName("ETB triggers may ability to exile artifact from hand")
    void etbTriggersImprintChoice() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting imprint exiles artifact from hand and imprints it")
    void acceptImprintExilesAndImprints() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        // Accept the may ability (inner effect resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // Should be awaiting card choice from hand
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.IMPRINT_FROM_HAND_CHOICE);

        // Choose the artifact (index 0 in remaining hand)
        harness.handleCardChosen(player1, 0);

        // Golem's Heart should be exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Golem's Heart"));

        // Golem's Heart should no longer be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Golem's Heart"));

        // Portal should have Golem's Heart imprinted
        Permanent portal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prototype Portal"))
                .findFirst().orElseThrow();
        assertThat(portal.getCard().getImprintedCard()).isNotNull();
        assertThat(portal.getCard().getImprintedCard().getName()).isEqualTo("Golem's Heart");
    }

    @Test
    @DisplayName("Declining imprint leaves artifact in hand")
    void declineImprintLeavesCardInHand() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Golem's Heart should still be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Golem's Heart"));

        // Portal should have nothing imprinted
        Permanent portal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prototype Portal"))
                .findFirst().orElseThrow();
        assertThat(portal.getCard().getImprintedCard()).isNull();
    }

    @Test
    @DisplayName("No artifacts in hand skips imprint gracefully")
    void noArtifactsInHandSkips() {
        harness.setHand(player1, List.of(new PrototypePortal(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Resolve Portal → MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect → may prompt

        // Accept may — but there are no artifacts in hand (inner effect resolves inline → no artifacts → skip)
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();

        // GrizzlyBears should still be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Portal should have nothing imprinted
        Permanent portal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prototype Portal"))
                .findFirst().orElseThrow();
        assertThat(portal.getCard().getImprintedCard()).isNull();
    }

    // ===== Token creation =====

    @Test
    @DisplayName("Activated ability creates a token copy of the imprinted artifact")
    void activateCreatesTokenCopy() {
        // Set up Portal with an imprinted artifact via addToBattlefield
        PrototypePortal portalCard = new PrototypePortal();
        GolemsHeart heartCard = new GolemsHeart();
        portalCard.setImprintedCard(heartCard);
        harness.addToBattlefield(player1, portalCard);

        // Golem's Heart has mana value 2, so X=2
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities(); // Resolve activated ability

        GameData gd = harness.getGameData();

        // A token copy of Golem's Heart should be on the battlefield
        Permanent tokenHeart = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem's Heart") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(tokenHeart).isNotNull();
    }

    @Test
    @DisplayName("Token is NOT exiled at end step (unlike Mimic Vat)")
    void tokenIsPermanent() {
        PrototypePortal portalCard = new PrototypePortal();
        AccordersShield shieldCard = new AccordersShield();
        portalCard.setImprintedCard(shieldCard);
        harness.addToBattlefield(player1, portalCard);

        // Accorder's Shield has mana value 0, so X=0
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities(); // Resolve activated ability

        GameData gd = harness.getGameData();

        // Token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Accorder's Shield") && p.getCard().isToken());

        // Advance to end step
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Token should still be on the battlefield (not exiled)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Accorder's Shield") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot activate ability when nothing is imprinted")
    void cannotActivateWhenNothingImprinted() {
        harness.addToBattlefield(player1, new PrototypePortal());

        // Per ruling: "You may not activate the second ability if no card has been exiled with Prototype Portal."
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card has been exiled with");
    }

    @Test
    @DisplayName("X must equal mana value of imprinted card")
    void xMustMatchManaValue() {
        PrototypePortal portalCard = new PrototypePortal();
        GolemsHeart heartCard = new GolemsHeart();
        portalCard.setImprintedCard(heartCard);
        harness.addToBattlefield(player1, portalCard);

        // Golem's Heart has mana value 2, so X=3 should fail
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must equal the mana value of the imprinted card");
    }
}
