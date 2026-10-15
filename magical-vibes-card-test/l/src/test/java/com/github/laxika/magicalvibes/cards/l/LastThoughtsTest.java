package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastThoughts.class, GrizzlyBears.class, TurnToFrog.class})
class LastThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when cipher is declined")
    void drawsACard() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LastThoughts()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.assertInGraveyard(player1, "Last Thoughts");
    }

    @Test
    @DisplayName("Casts a cipher copy after encoded creature deals combat damage")
    void castsCipherCopy() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LastThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Last Thoughts"));
        harness.assertNotInGraveyard(player1, "Last Thoughts");

        int handSizeAfterEncoding = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterEncoding + 1);
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    @DisplayName("Cannot encode when only the opponent controls a creature")
    void cannotEncodeOnOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LastThoughts()));
        harness.setLibrary(player1, List.of(new LastThoughts(), new LastThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Last Thoughts");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May decline casting the encoded copy without losing the encoded card")
    void mayDeclineCipherCopy() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LastThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertNotInGraveyard(player1, "Last Thoughts");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cipher does not trigger after the encoded creature loses all abilities")
    void abilityLossSuppressesCipherTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LastThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defendingLife - 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.exiledCards).hasSize(1);
    }
}
