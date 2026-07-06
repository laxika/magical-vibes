package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprovisationCapstone;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmCastSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EchocastingSymposiumTest extends BaseCardTest {

    

    @Test
    @DisplayName("Target player creates a token copy of target creature you control")
    void targetPlayerCreatesTokenCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(player2.getId(), bearId));
        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).hasSize(1);
        assertThat(opponentBf.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(opponentBf.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Cast via Improvisation Capstone prompts player then creature, in that order")
    void castViaImprovisationCapstonePromptsPlayerThenCreature() {
        EchocastingSymposium echo = new EchocastingSymposium();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(echo));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(echo.getId()));

        // First slot is the target player: players are offered, the creature is not.
        PendingInteraction.PermanentChoice playerPrompt =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerPrompt.validIds()).contains(player2.getId()).doesNotContain(bearId);
        harness.handlePermanentChosen(player1, player2.getId());

        // Second slot is the creature you control: the bear is offered, players are not.
        PendingInteraction.PermanentChoice creaturePrompt =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creaturePrompt.validIds()).contains(bearId).doesNotContain(player2.getId());
        harness.handlePermanentChosen(player1, bearId);

        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A multi-target spell mid-queue still lets Improvisation Capstone cast the rest")
    void multiTargetSpellMidQueueResumesRemainingCasts() {
        EchocastingSymposium echo = new EchocastingSymposium();
        GrizzlyBears queuedCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new GrizzlyBears());
        // The low mana value creature is exiled first so Improvisation Capstone keeps exiling until it
        // reaches Echocasting; both then land in exile. The chosen queue casts Echocasting (multi-
        // target) first, so the queued creature must be cast once Echocasting finishes its targets.
        harness.setLibrary(player1, List.of(queuedCreature, echo));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(echo.getId(), queuedCreature.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bearId);

        // The queued creature spell was cast after the multi-target spell finished its targets.
        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getId().equals(queuedCreature.getId()))).isTrue();
    }

    @Test
    @DisplayName("Paradigm copy cast from exile prompts both targets and creates the token copy")
    void paradigmCopyCastFromExileCreatesTokenCopy() {
        ParadigmCastSupport paradigmCastSupport =
                GameTestEngineContext.get().getBean(ParadigmCastSupport.class);

        EchocastingSymposium copy = new EchocastingSymposium();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setExile(player1, List.of(copy));

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        paradigmCastSupport.castFromExileWithoutPaying(gd, player1, copy.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, bearId);

        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Grizzly Bears"));

        // The Paradigm copy ceases to exist (CR 707.10a) — it is not in the graveyard, exile, or stack.
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(copy.getId()));
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(copy.getId()));
        assertThat(gd.stack).noneMatch(e -> e.getCard().getId().equals(copy.getId()));
    }

    @Test
    @DisplayName("Paradigm copy with no creature you control ceases to exist without prompting")
    void paradigmCopyWithoutLegalTargetsCeasesToExist() {
        ParadigmCastSupport paradigmCastSupport =
                GameTestEngineContext.get().getBean(ParadigmCastSupport.class);

        EchocastingSymposium copy = new EchocastingSymposium();
        harness.setExile(player1, List.of(copy));

        paradigmCastSupport.castFromExileWithoutPaying(gd, player1, copy.getId());

        // No creature the caster controls → no legal target set, so nothing is prompted and the
        // copy ceases to exist rather than being put into a graveyard.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(copy.getId()));
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(copy.getId()));
        assertThat(gd.stack).noneMatch(e -> e.getCard().getId().equals(copy.getId()));
    }
}
