package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonSmiter.class, Cancel.class, Distress.class, GrizzlyBears.class, MindRot.class, RavenousRats.class, Sift.class})
class LoxodonSmiterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        LoxodonSmiter smiter = new LoxodonSmiter();
        harness.setHand(player1, List.of(smiter));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, smiter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Smiter");
        harness.assertNotInGraveyard(player1, "Loxodon Smiter");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Distress")
    void entersBattlefieldWhenDiscardedByOpponentViaDistress() {
        harness.setHand(player2, List.of(new LoxodonSmiter()));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
    }

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Mind Rot")
    void entersBattlefieldWhenDiscardedByOpponentViaMindRot() {
        harness.setHand(player2, List.of(new LoxodonSmiter(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
    }

    @Test
    @DisplayName("Does NOT enter battlefield when controller discards it themselves")
    void doesNotEnterBattlefieldOnSelfDiscard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Sift(), new LoxodonSmiter()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Loxodon Smiter");
        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
    }

    @Test
    @DisplayName("Hand is empty after Smiter enters via Distress")
    void handEmptyAfterDistressReplacement() {
        harness.setHand(player2, List.of(new LoxodonSmiter()));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Self-targeted Mind Rot does not apply the opponent discard replacement")
    void selfTargetedMindRotDiscardsSmiterNormally() {
        harness.setHand(player1, List.of(new MindRot(), new LoxodonSmiter()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Loxodon Smiter");
        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mind Rot replaces both Smiter discards without creating stack entries")
    void bothSmitersEnterFromOpponentMindRot() {
        LoxodonSmiter first = new LoxodonSmiter();
        LoxodonSmiter second = new LoxodonSmiter();
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's triggered discard ability puts Smiter onto the battlefield")
    void entersBattlefieldFromOpponentTriggeredAbility() {
        harness.setHand(player2, List.of(new LoxodonSmiter()));
        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
