package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimulacrumSynthesizer.class, Manalith.class, MindStone.class, HillGiant.class,
        GrizzlyBears.class})
class SimulacrumSynthesizerTest extends BaseCardTest {

    @Test
    void entersAndScriesTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SimulacrumSynthesizer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void qualifyingArtifactCreatesConstructThatScalesWithArtifacts() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.addToBattlefield(player1, new MindStone());
        harness.setHand(player1, List.of(new Manalith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct).isNotNull();
        assertThat(construct.getCard().isToken()).isTrue();
        assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);
    }

    @Test
    void artifactWithManaValueBelowThreeDoesNotCreateConstruct() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonartifactWithManaValueAtLeastThreeDoesNotCreateConstruct() {
        harness.addToBattlefield(player1, new SimulacrumSynthesizer());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Construct")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
