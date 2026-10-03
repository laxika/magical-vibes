package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArgentMutation.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class,
        PristineTalisman.class})
class ArgentMutationTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting puts it on the stack with target")
    void castingPutsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving makes target permanent an artifact and draws a card")
    void makesTargetArtifactAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(target.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can target a land and make it an artifact")
    void canTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(target.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("Can target an enchantment and make it an artifact")
    void canTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(target.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("Artifact type wears off at end of turn")
    void artifactTypeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gqs.isArtifact(target)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getGrantedCardTypes()).isEmpty();
        assertThat(gqs.isArtifact(target)).isFalse();
    }

    @Test
    @DisplayName("Fizzles and does not draw when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = target.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Argent Mutation");
    }

    @Test
    @DisplayName("Can target your own permanent and draws for the caster")
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("An existing artifact is a legal target and remains an artifact after cleanup")
    void canTargetExistingArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());
        harness.setHand(player1, List.of(new ArgentMutation()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
    }
}
