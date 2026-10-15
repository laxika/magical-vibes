package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoransEscape.class, GrizzlyBears.class, HowlingMine.class, Forest.class})
class LoransEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains hexproof and indestructible and the controller scries 1")
    void protectsCreatureAndScries() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Can target a noncreature artifact")
    void protectsArtifact() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Howling Mine");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent mine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mine.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(mine.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Completing scry sends the spell to the graveyard")
    void completingScryFinishesResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Loran's Escape");
    }

    @Test
    void protectsOpponentsCreatureAndOnlyCasterScries() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        harness.assertInGraveyard(player1, "Loran's Escape");
    }

    @Test
    void canPutScryedCardOnBottom() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest topCard = new Forest();
        GrizzlyBears nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        harness.assertInGraveyard(player1, "Loran's Escape");
    }

    @Test
    void protectionExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Loran's Escape");
    }

    @Test
    @CardUsed({GoForTheThroat.class})
    void indestructibleStopsControllersDestroySpellAndHexproofStopsOpponentTargeting() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LoransEscape(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Go for the Throat");

        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void doesNotScryWhenTargetGainsOpposingHexproofBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new LoransEscape()));
        harness.setHand(player2, List.of(new LoransEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Loran's Escape");
        harness.assertInGraveyard(player2, "Loran's Escape");
    }
}
