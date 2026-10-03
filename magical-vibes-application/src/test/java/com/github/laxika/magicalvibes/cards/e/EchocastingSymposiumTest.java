package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AzizaMageTowerCaptain;
import com.github.laxika.magicalvibes.cards.h.HoodedHydra;
import com.github.laxika.magicalvibes.cards.i.ImprovisationCapstone;
import com.github.laxika.magicalvibes.cards.n.NoxiousNewt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.paradigm.ParadigmCastSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchocastingSymposium.class, NoxiousNewt.class, ImprovisationCapstone.class,
        AzizaMageTowerCaptain.class, HoodedHydra.class})
class EchocastingSymposiumTest extends BaseCardTest {

    @Test
    @DisplayName("Target player creates a token copy of target creature you control")
    void targetPlayerCreatesTokenCopy() {
        UUID newtId = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt()).getId();
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(player2.getId(), newtId));
        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).hasSize(1);
        assertThat(opponentBf.getFirst().getCard().getName()).isEqualTo("Noxious Newt");
        assertThat(opponentBf.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Cast via Improvisation Capstone prompts player then creature, in that order")
    void castViaImprovisationCapstonePromptsPlayerThenCreature() {
        EchocastingSymposium echo = new EchocastingSymposium();
        UUID newtId = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt()).getId();
        harness.setLibrary(player1, List.of(echo));
        harness.castFromHand(player1, new ImprovisationCapstone(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(echo.getId()));

        // First slot is the target player: players are offered, the creature is not.
        PendingInteraction.PermanentChoice playerPrompt =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerPrompt.validIds()).contains(player2.getId()).doesNotContain(newtId);
        harness.handlePermanentChosen(player1, player2.getId());

        // Second slot is the creature you control: the newt is offered, players are not.
        PendingInteraction.PermanentChoice creaturePrompt =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creaturePrompt.validIds()).contains(newtId).doesNotContain(player2.getId());
        harness.handlePermanentChosen(player1, newtId);

        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Noxious Newt"));
    }

    @Test
    @DisplayName("A multi-target spell mid-queue still lets Improvisation Capstone cast the rest")
    void multiTargetSpellMidQueueResumesRemainingCasts() {
        EchocastingSymposium echo = new EchocastingSymposium();
        NoxiousNewt queuedCreature = new NoxiousNewt();
        UUID newtId = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt()).getId();
        // The low mana value creature is exiled first so Improvisation Capstone keeps exiling until it
        // reaches Echocasting; both then land in exile. The chosen queue casts Echocasting (multi-
        // target) first, so the queued creature must be cast once Echocasting finishes its targets.
        harness.setLibrary(player1, List.of(queuedCreature, echo));
        harness.castFromHand(player1, new ImprovisationCapstone(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(echo.getId(), queuedCreature.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, newtId);

        // The queued creature spell was cast after the multi-target spell finished its targets.
        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getId().equals(queuedCreature.getId()))).isTrue();
    }

    @Test
    @DisplayName("Paradigm copy cast from exile prompts both targets and creates the token copy")
    void paradigmCopyCastFromExileCreatesTokenCopy() {
        ParadigmCastSupport paradigmCastSupport =
                GameTestEngineContext.get().getBean(ParadigmCastSupport.class);

        EchocastingSymposium copy = new EchocastingSymposium();
        UUID newtId = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt()).getId();
        harness.setExile(player1, List.of(copy));

        harness.inMutationScope(() -> paradigmCastSupport.castFromExileWithoutPaying(gd, player1, copy.getId()));

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, newtId);

        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Noxious Newt"));

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

        harness.inMutationScope(() -> paradigmCastSupport.castFromExileWithoutPaying(gd, player1, copy.getId()));

        // No creature the caster controls → no legal target set, so nothing is prompted and the
        // copy ceases to exist rather than being put into a graveyard.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(copy.getId()));
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(copy.getId()));
        assertThat(gd.stack).noneMatch(e -> e.getCard().getId().equals(copy.getId()));
    }

    @Test
    void casterCanCreateTheCopyForThemselves() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(player1.getId(), original.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Noxious Newt"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Echocasting Symposium"));
        harness.assertNotInGraveyard(player1, "Echocasting Symposium");
    }

    @Test
    void paradigmCanBeDeclinedAndThenCastOnALaterFirstMainPhase() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setLibrary(player1, List.of(new NoxiousNewt(), new NoxiousNewt(), new NoxiousNewt()));
        harness.setLibrary(player2, List.of(new NoxiousNewt(), new NoxiousNewt(), new NoxiousNewt()));
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, List.of(player2.getId(), original.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, original.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertNotInGraveyard(player1, "Echocasting Symposium");
    }

    @Test
    void creatureThatChangesControllerCannotBeCopiedButSpellStillResolves() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, List.of(player2.getId(), original.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(original);
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Echocasting Symposium"));
        harness.assertNotInGraveyard(player1, "Echocasting Symposium");
    }

    @Test
    void multicoloredCreatureCopyRetainsEveryColor() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new AzizaMageTowerCaptain());
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, List.of(player2.getId(), original.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }

    @Test
    void firstResolvedSpellCopyRegistersParadigmBeforeOriginalResolves() {
        harness.addToBattlefield(player1, new AzizaMageTowerCaptain());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        harness.addToBattlefield(player1, new NoxiousNewt());
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(player2.getId(), original.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Noxious Newt"));
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
    }

    @Test
    void faceDownCreatureIsCopiedAsAnAbilitylessTwoTwo() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent original = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(original.isFaceDown()).isTrue();

        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, List.of(player2.getId(), original.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getName()).isNullOrEmpty();
        assertThat(token.getCard().getManaCost()).isNullOrEmpty();
    }
}
