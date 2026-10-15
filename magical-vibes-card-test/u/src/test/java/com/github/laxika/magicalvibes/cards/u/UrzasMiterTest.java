package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasMiter.class, MindStone.class, Naturalize.class, Atog.class})
class UrzasMiterTest extends BaseCardTest {

    @Test
    @DisplayName("A destroyed artifact offers payment to draw a card")
    void destroyedArtifactOffersPaymentToDraw() {
        harness.addToBattlefield(player1, new UrzasMiter());
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mind Stone"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCast);
    }

    @Test
    @DisplayName("A sacrificed artifact does not trigger Urza's Miter")
    void sacrificedArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new UrzasMiter());
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(atog), null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Urza's Miter triggers when it is itself destroyed")
    void selfDestructionOffersPayment() {
        harness.addToBattlefield(player1, new UrzasMiter());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new Atog()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Urza's Miter"));

        harness.assertInGraveyard(player1, "Urza's Miter");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Atog");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining payment does not draw a card")
    void decliningPaymentDoesNotDraw() {
        harness.addToBattlefield(player1, new UrzasMiter());
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new Atog()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mind Stone"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's destroyed artifact does not trigger Urza's Miter")
    void opponentsArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new UrzasMiter());
        harness.addToBattlefield(player2, new MindStone());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Mind Stone"));

        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana cannot buy a card")
    void insufficientManaDoesNotDraw() {
        harness.addToBattlefield(player1, new UrzasMiter());
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new Atog()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mind Stone"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana draws exactly one card")
    void payingDrawsExactlyOneCard() {
        harness.addToBattlefield(player1, new UrzasMiter());
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new Atog(), new Atog()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mind Stone"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Atog");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
