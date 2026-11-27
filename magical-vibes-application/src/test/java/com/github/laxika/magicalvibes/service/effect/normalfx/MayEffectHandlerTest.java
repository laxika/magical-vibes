package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MayEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    void triggeringPermanentCurrentControllerMakesTheChoice() {
        MayEffect effect = new MayEffect(new DrawCardEffect(1), "Draw?", null,
                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER);
        StackEntry entry = createEntryWithTarget(createCard("Source"), player1Id, List.of(effect), player1Id);
        UUID triggeringId = UUID.randomUUID();
        entry.setTriggeringPermanentId(triggeringId);
        entry.setTriggeringPermanentControllerId(player1Id);
        when(gameQueryService.findPermanentController(gd, triggeringId)).thenReturn(player2Id);

        resolveEffect(gd, entry, effect);

        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
        assertThat(entry.getControllerId()).isEqualTo(player1Id);
    }

    @Test
    @DisplayName("Asks nothing for an effect that acts only on its targets when none was chosen")
    void skipsEffectActingOnlyOnItsTargetsWithoutTargets() {
        MayEffect mayEffect = new MayEffect(
                AttachSourceEquipmentToTargetCreatureEffect.targetEquipmentToTriggeringOrSource(), "Attach?");
        StackEntry entry = createEntry(createCard("Armory Automaton"), player1Id, List.of(mayEffect));

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("Asks nothing for a graveyard return when no target card was chosen")
    void skipsGraveyardReturnWithoutTargetCards() {
        MayEffect mayEffect = new MayEffect(new ReturnTargetCardsFromGraveyardToHandEffect(null, 1), "Return?");
        StackEntry entry = createEntry(createCard("Tilling Treefolk"), player1Id, List.of(mayEffect));

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("Asks for a graveyard return when a target card was chosen")
    void asksForGraveyardReturnWithATargetCard() {
        ReturnTargetCardsFromGraveyardToHandEffect returnCard = new ReturnTargetCardsFromGraveyardToHandEffect(null, 1);
        MayEffect mayEffect = new MayEffect(returnCard, "Return?");
        StackEntry entry = createEntry(createCard("Tilling Treefolk"), player1Id, List.of(mayEffect));
        entry.setTargetCardIds(List.of(UUID.randomUUID()));

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(returnCard);
    }

    @Test
    @DisplayName("Asks nothing for a graveyard-choosing effect whose choice took no card")
    void skipsGraveyardChoosingEffectWithoutChosenCards() {
        MayEffect mayEffect = new MayEffect(
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD), "Exile?");
        // "That player" of a combat damage trigger is the entry's non-targeting referent, not a chosen card.
        StackEntry entry = createEntryWithTarget(createCard("Zombie Cannibal"), player1Id, List.of(mayEffect),
                player2Id);
        entry.setNonTargeting(true);

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("Asks for a graveyard-choosing effect whose choice took a card")
    void asksForGraveyardChoosingEffectWithAChosenCard() {
        ExileGraveyardCardsEffect exile =
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD);
        MayEffect mayEffect = new MayEffect(exile, "Exile?");
        StackEntry entry = createEntry(createCard("Zombie Cannibal"), player1Id, List.of(mayEffect));
        entry.setTargetCardIds(List.of(UUID.randomUUID()));

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(exile);
    }

    @Test
    @DisplayName("Asks nothing for a control gift whose permanent target is gone")
    void skipsControlGiftWithoutItsPermanent() {
        GainControlOfTargetEffect gift = new GainControlOfTargetEffect(Duration.PERMANENT)
                .gainedBy(Subjects.targetPlayer(0));
        MayEffect mayEffect = new MayEffect(gift, "Give it away?");
        StackEntry entry = createEntry(createCard("Iroh, Tea Master"), player1Id, List.of(mayEffect));
        UUID permanentId = UUID.randomUUID();
        entry.chooseTargetIds(List.of(player2Id, permanentId));

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("Asks for a control gift whose player and permanent targets are both there")
    void asksForControlGiftWithBothTargets() {
        GainControlOfTargetEffect gift = new GainControlOfTargetEffect(Duration.PERMANENT)
                .gainedBy(Subjects.targetPlayer(0));
        MayEffect mayEffect = new MayEffect(gift, "Give it away?");
        StackEntry entry = createEntry(createCard("Iroh, Tea Master"), player1Id, List.of(mayEffect));
        UUID permanentId = UUID.randomUUID();
        entry.chooseTargetIds(List.of(player2Id, permanentId));
        Permanent bear = new Permanent(createCard("Bear"));
        when(gameQueryService.findPermanentById(eq(gd), any(UUID.class)))
                .thenAnswer(invocation -> permanentId.equals(invocation.getArgument(1)) ? bear : null);

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(gift);
    }

    @Test
    @DisplayName("Asks for an effect that acts only on its targets when one was chosen")
    void asksForEffectActingOnlyOnItsTargetsWithATarget() {
        AttachSourceEquipmentToTargetCreatureEffect attach =
                AttachSourceEquipmentToTargetCreatureEffect.targetEquipmentToTriggeringOrSource();
        MayEffect mayEffect = new MayEffect(attach, "Attach?");
        StackEntry entry = createEntryWithTarget(createCard("Armory Automaton"), player1Id, List.of(mayEffect),
                UUID.randomUUID());

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(attach);
    }

    @Test
            @DisplayName("Sets resolvingMayEffectFromStack flag and adds pending may ability")
            void setsFlagAndAddsPendingMay() {
                Card card = createCard("Ob Nixilis");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                MayEffect mayEffect = new MayEffect(wrapped, "Draw a card?");
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayEffect), player2Id);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.resolvingMayEffectFromStack).isTrue();
                assertThat(gd.pendingMayAbilities).hasSize(1);
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
                assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(wrapped);
            }

            @Test
            @DisplayName("Preserves target and source permanent IDs in pending may")
            void preservesTargetAndSourceIds() {
                Card card = createCard("Ob Nixilis");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                MayEffect mayEffect = new MayEffect(wrapped, "Draw a card?");
                UUID sourcePermanentId = UUID.randomUUID();
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(mayEffect), player2Id, sourcePermanentId);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().targetCardId()).isEqualTo(player2Id);
                assertThat(gd.pendingMayAbilities.getFirst().sourcePermanentId()).isEqualTo(sourcePermanentId);
            }

            @Test
            @DisplayName("Carries the entry's granting permanent into the pending may ability")
            void carriesGrantingPermanent() {
                MayEffect mayEffect = new MayEffect(new DrawCardEffect(1), "Draw a card?");
                StackEntry entry = createEntryWithTarget(createCard("Spare Dagger"), player1Id, List.of(mayEffect),
                        player2Id);
                UUID grantingPermanentId = UUID.randomUUID();
                entry.setGrantingPermanentId(grantingPermanentId);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().grantingPermanentId()).isEqualTo(grantingPermanentId);
            }

            @Test
            @DisplayName("Carries the entry's triggering spell into the pending may ability")
            void carriesTriggeringSpell() {
                MayEffect mayEffect = new MayEffect(new DrawCardEffect(1), "Draw a card?");
                StackEntry entry = createEntryWithTarget(createCard("Swindler's Scheme"), player1Id,
                        List.of(mayEffect), player2Id);
                Card spellCard = createCard("Opponent Spell");
                entry.setTriggeringSpell(new StackEntry(spellCard, player2Id));

                resolveEffect(gd, entry, mayEffect);

                StackEntry carried = gd.pendingMayAbilities.getFirst().triggeringSpellSnapshot();
                assertThat(carried.getCard()).isSameAs(spellCard);
                assertThat(carried.getControllerId()).isEqualTo(player2Id);
            }

            @Test
            @DisplayName("Offers a MayEffect with a decline branch to the target permanent's controller")
            void offersChoiceToTargetPermanentController() {
                Card card = createCard("Blazing Salvo");
                DrawCardEffect accepted = new DrawCardEffect(1);
                GainLifeEffect declined = new GainLifeEffect(1);
                MayEffect mayEffect = new MayEffect(accepted, "Choose?", declined,
                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER);
                UUID targetPermanentId = UUID.randomUUID();
                when(gameQueryService.findPermanentController(gd, targetPermanentId)).thenReturn(player2Id);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayEffect), targetPermanentId);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
                assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(accepted);
            }

            @Test
            @DisplayName("Chooses the controller of the targeted spell")
            void choosesTargetSpellController() {
                Card source = createCard("Vex");
                Card targetSpell = createCard("Grizzly Bears");
                StackEntry targetEntry = createEntry(targetSpell, player2Id, List.of());
                gd.stack.add(targetEntry);

                MayEffect mayEffect = new MayEffect(
                        new DrawCardEffect(1),
                        "Draw a card?",
                        null,
                        MayChoicePlayer.TARGET_SPELL_CONTROLLER);
                StackEntry entry = createEntryWithTarget(source, player1Id, List.of(mayEffect), targetSpell.getId());

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
            }

    @Test
    @DisplayName("Asks nothing for \"you may sacrifice one of them\" when none of them can be sacrificed")
    void skipsSacrificeChoiceWithNothingSacrificeable() {
        ChoosePermanentsEffectHandler chooser = mock(ChoosePermanentsEffectHandler.class);
        ReflectionTestUtils.setField(registry.getHandler(new MayEffect(new GainLifeEffect(1), "x")),
                "choosePermanentsEffectHandler", chooser);
        ChoosePermanentsEffect choose = ChoosePermanentsEffect.exactly(1, Subjects.triggeringAll(), "one of them")
                .toSacrifice();
        MayEffect mayEffect = new MayEffect(SequenceEffect.of(choose,
                new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE))), "Sacrifice one of them?");
        StackEntry entry = createEntry(createCard("Descendants' Fury"), player1Id, List.of(mayEffect));
        when(chooser.couldSacrificeAny(gd, entry, choose)).thenReturn(false);

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.resolvingMayEffectFromStack).isFalse();
    }

    @Test
    @DisplayName("Asks \"you may sacrifice one of them\" when one of them can be sacrificed")
    void asksSacrificeChoiceWithASacrificeableCandidate() {
        ChoosePermanentsEffectHandler chooser = mock(ChoosePermanentsEffectHandler.class);
        ReflectionTestUtils.setField(registry.getHandler(new MayEffect(new GainLifeEffect(1), "x")),
                "choosePermanentsEffectHandler", chooser);
        ChoosePermanentsEffect choose = ChoosePermanentsEffect.exactly(1, Subjects.triggeringAll(), "one of them")
                .toSacrifice();
        MayEffect mayEffect = new MayEffect(SequenceEffect.of(choose,
                new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE))), "Sacrifice one of them?");
        StackEntry entry = createEntry(createCard("Descendants' Fury"), player1Id, List.of(mayEffect));
        when(chooser.couldSacrificeAny(gd, entry, choose)).thenReturn(true);

        resolveEffect(gd, entry, mayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
    }
}
