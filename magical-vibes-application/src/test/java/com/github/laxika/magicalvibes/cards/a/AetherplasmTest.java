package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aetherplasm.class, GhostWarden.class, IzzetSignet.class, Repeal.class})
class AetherplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the first may choice leaves Aetherplasm blocking")
    void decliningReturnKeepsAetherplasmBlocking() {
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent aetherplasm = addCreatureReady(player2, new Aetherplasm());

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aetherplasm);
        assertThat(aetherplasm.isBlocking()).isTrue();
        assertThat(aetherplasm.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Returning Aetherplasm allows declining the replacement creature")
    void returningAetherplasmCanDeclineReplacement() {
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent aetherplasm = addCreatureReady(player2, new Aetherplasm());
        Card replacementCard = new GhostWarden();
        harness.setHand(player2, List.of(replacementCard));

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherplasm);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(aetherplasm.getCard(), replacementCard);
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("A creature chosen from hand enters blocking the same attacker")
    void replacementCreatureEntersBlocking() {
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent aetherplasm = addCreatureReady(player2, new Aetherplasm());
        Card replacementCard = new GhostWarden();
        harness.setHand(player2, List.of(replacementCard));

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        Permanent replacement = findPermanent(player2, "Ghost Warden");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherplasm);
        assertThat(replacement).isNotNull();
        assertThat(replacement.isBlocking()).isTrue();
        assertThat(replacement.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
    }

    @Test
    @DisplayName("Returning Aetherplasm cannot choose a noncreature replacement")
    void noncreatureReplacementIsNotEligible() {
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent aetherplasm = addCreatureReady(player2, new Aetherplasm());
        Card noncreatureCard = new IzzetSignet();
        Card replacementCard = new GhostWarden();
        harness.setHand(player2, List.of(noncreatureCard, replacementCard));

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).doesNotContain(0).contains(1);
        harness.handleCardChosen(player2, 1);

        Permanent replacement = findPermanent(player2, "Ghost Warden");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherplasm);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(aetherplasm.getCard(), noncreatureCard);
        assertThat(replacement.isBlocking()).isTrue();
        assertThat(replacement.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
    }

    @Test
    @DisplayName("Aetherplasm can return itself as the replacement without triggering again")
    void returnedAetherplasmCanEnterBlockingWithoutAnotherTrigger() {
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent aetherplasm = addCreatureReady(player2, new Aetherplasm());
        harness.setHand(player2, List.of());

        declareBlock(attacker, aetherplasm);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        Permanent replacement = findPermanent(player2, "Aetherplasm");
        assertThat(replacement.getId()).isNotEqualTo(aetherplasm.getId());
        assertThat(replacement.isBlocking()).isTrue();
        assertThat(replacement.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Aetherplasm.class, GhostWarden.class, Repeal.class})
    @DisplayName("Aetherplasm removed in response cannot put a replacement onto the battlefield")
    void cannotReplaceAetherplasmWhenItWasAlreadyReturnedInResponse() {
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent aetherplasm = addCreatureReady(player2, new Aetherplasm());
        Card replacementCard = new GhostWarden();
        harness.setHand(player2, List.of(replacementCard));
        harness.setHand(player1, List.of(new Repeal()));
        harness.setLibrary(player1, List.of(new GhostWarden()));

        declareBlock(attacker, aetherplasm);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, 4, aetherplasm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherplasm);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(aetherplasm.getCard(), replacementCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
