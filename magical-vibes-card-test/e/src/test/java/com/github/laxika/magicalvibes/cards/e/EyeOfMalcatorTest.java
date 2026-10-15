package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BasilicaSkullbomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfMalcator.class, Forest.class, BasilicaSkullbomb.class})
class EyeOfMalcatorTest extends BaseCardTest {

    @Test
    @DisplayName("Eye of Malcator's enters-the-battlefield ability starts a scry 2 interaction")
    void entersTheBattlefieldScriesTwo() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EyeOfMalcator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getFirst())).isFalse();
    }

    @Test
    @DisplayName("Another artifact entering under your control animates Eye of Malcator")
    void anotherAllyArtifactEntryAnimatesEye() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.setHand(player1, List.of(new BasilicaSkullbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, eye)).isTrue();
        assertThat(gqs.isArtifact(eye)).isTrue();
        assertThat(gqs.getEffectivePower(gd, eye)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eye)).isEqualTo(4);
        assertThat(eye.getTransientSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.EYE);
    }

    @Test
    @DisplayName("Eye of Malcator's animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.setHand(player1, List.of(new BasilicaSkullbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, eye)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, eye)).isFalse();
        assertThat(eye.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact does not animate Eye of Malcator")
    void opponentArtifactDoesNotAnimateEye() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BasilicaSkullbomb()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, eye)).isFalse();
    }

    @Test
    @DisplayName("A nonartifact entering does not animate Eye of Malcator")
    void nonartifactDoesNotAnimateEye() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, eye)).isFalse();
    }

    @Test
    @DisplayName("One artifact entry animates each Eye without animating the entering artifact")
    void artifactEntryAnimatesEachEye() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.setHand(player1, List.of(new BasilicaSkullbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, first)).isTrue();
        assertThat(gqs.isCreature(gd, second)).isTrue();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getLast())).isFalse();
    }

    @Test
    @DisplayName("Scry two can put one card on the bottom and keep the other on top")
    void scryCanSplitTopAndBottom() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new EyeOfMalcator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getFirst())).isFalse();
    }

    @Test
    @DisplayName("Scry two with only one card in the library offers that card")
    void scryWithOneCard() {
        Forest onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new EyeOfMalcator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Scry two with an empty library finishes without requesting input")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EyeOfMalcator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, gd.playerBattlefields.get(player1.getId()).getFirst())).isFalse();
    }
}
