package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.replacement.TokenCopyBatch;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Shared token-copy creation used by target-copy effects and last-known-information riders. */
@Component
@RequiredArgsConstructor
public class TokenCopySupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final SagaChapterService sagaChapterService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.aura.AuraAttachmentService auraAttachmentService;
    private final ObjectProvider<AnimationSupport> animationSupport;
    private final ObjectProvider<ReplacementOrderService> replacementOrderService;

    public List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                        Permanent sourcePermanent,
                                        CreateTokenCopyOfTargetPermanentEffect effect) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, entry.getControllerId(), effect,
                null);
    }

    public List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                        Permanent sourcePermanent, UUID tokenControllerId,
                                        CreateTokenCopyOfTargetPermanentEffect effect) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId, effect, null);
    }

    public List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                        Permanent sourcePermanent, UUID tokenControllerId,
                                        CreateTokenCopyOfTargetPermanentEffect effect,
                                        List<UUID> attackTargetIds) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId, effect,
                attackTargetIds, null);
    }

    /**
     * Creates token copies while applying a copy exception to each copied token card before it
     * enters. This keeps special copy exceptions on the shared token-copy pipeline.
     */
    public List<UUID> createTokenCopiesWithCopyException(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                         Permanent sourcePermanent, UUID tokenControllerId,
                                                         CreateTokenCopyOfTargetPermanentEffect effect,
                                                         Consumer<Card> copyException) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId, effect,
                null, copyException);
    }

    /**
     * Creates token copies with a copy exception, also setting up each copy's permanent with {@code copyRider}
     * before it enters, for state that belongs to the created tokens but isn't part of their copiable values (so a
     * copy of one of the tokens doesn't get it). Added tokens such as Queen Allenal's Soldier don't get it.
     */
    public List<UUID> createTokenCopiesWithCopyException(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                         Permanent sourcePermanent, UUID tokenControllerId,
                                                         CreateTokenCopyOfTargetPermanentEffect effect,
                                                         Consumer<Card> copyException,
                                                         Consumer<Permanent> copyRider) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId,
                effect, null, copyException,
                copyRider == null ? null : (copiedCard, copy) -> copyRider.accept(copy));
    }

    /**
     * Creates a copy of each of {@code sourceCards} as one ordered token creation event (CR 616.1), setting up every
     * token the event creates with {@code tokenRider} before it enters: the copies and the tokens a replacement adds
     * or substitutes (Queen Allenal's Soldier, Jinnie Fay's Cats or Dogs) alike, since the creating effect's
     * instructions apply to all of them. The ids are only returned when the creation doesn't pause for a choice.
     */
    public List<UUID> createTokenCopiesWithTokenRider(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                      UUID tokenControllerId,
                                                      CreateTokenCopyOfTargetPermanentEffect effect,
                                                      Consumer<Permanent> tokenRider) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return List.of();
        }
        return createOrderedTokenCopies(gameData, entry, sourceCards, null, tokenControllerId, effect, null, null,
                null, tokenRider, null, false, null);
    }

    /**
     * Creates a copy of each of {@code sourceCards} as one ordered token creation event (CR 616.1), setting up every
     * token the event creates with {@code instructionRider} before it enters (given the card the token was created
     * for and its permanent, as {@link TokenCopyBatch#instructionRider()} describes: {@code null} for an added token,
     * the replaced copy's card for a Jinnie Fay substitute), and then running {@code afterEntry} with the game and
     * every token the event created, added and substituted tokens included, once they have all entered. {@code afterEntry} is the continuation for the
     * creating effect's follow-up work on its tokens, since the creation may pause for the replacement order,
     * Jinnie Fay or an Aura attachment choice and then finish on another call; it runs exactly once, on whichever
     * call the tokens enter, and must look permanents up through the given game rather than capture them.
     */
    public void createTokenCopiesThen(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                      UUID tokenControllerId, CreateTokenCopyOfTargetPermanentEffect effect,
                                      BiConsumer<Card, Permanent> instructionRider,
                                      BiConsumer<GameData, List<Permanent>> afterEntry) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return;
        }
        createOrderedTokenCopies(gameData, entry, sourceCards, null, tokenControllerId, effect, null, null,
                null, null, instructionRider, false, afterEntry);
    }

    /**
     * Creates {@code amount} copies of {@code sourceCard} (the creating effect's own source, such as an embalm or
     * eternalize copy) as one ordered token creation event (CR 616.1), applying {@code copyException} to each copy as
     * it is built. With {@code chooseAttackTargets}, the creating player chooses the defending player or planeswalker
     * each creature token attacks as it enters, as {@link #createTokenCopiesChoosingAttackTargets} does. Then runs
     * {@code afterEntry} as {@link #createTokenCopiesThen} does.
     */
    public void createSourceTokenCopiesThen(GameData gameData, StackEntry entry, Card sourceCard, int amount,
                                            Permanent sourcePermanent, CreateTokenCopyOfTargetPermanentEffect effect,
                                            Consumer<Card> copyException, boolean chooseAttackTargets,
                                            BiConsumer<GameData, List<Permanent>> afterEntry) {
        if (sourceCard == null || amount <= 0) {
            return;
        }
        createOrderedTokenCopies(gameData, entry, Collections.nCopies(amount, sourceCard), sourcePermanent,
                entry.getControllerId(), effect, copyException, null, null, null, null, chooseAttackTargets,
                afterEntry);
    }

    /**
     * Creates, for each of {@code opponentIds}, a copy of each of {@code sourceCards} as one ordered token creation
     * event (CR 616.1), for "for each opponent, create a token copy that [does something with] that opponent" (encore).
     * Every token the event creates is set up with {@code tokenRider} and then with {@code opponentInstruction},
     * given the opponent the token was created for, before it enters: the copies a doubler adds and Jinnie Fay's
     * substitutes get the opponent of the copy they double or replace, and tokens a replacement adds (Queen Allenal's
     * Soldier) the first opponent.
     */
    public void createTokenCopiesForEachOpponent(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                 UUID tokenControllerId,
                                                 CreateTokenCopyOfTargetPermanentEffect effect,
                                                 List<UUID> opponentIds, Consumer<Permanent> tokenRider,
                                                 BiConsumer<UUID, Permanent> opponentInstruction) {
        if (sourceCards == null || sourceCards.isEmpty() || opponentIds == null || opponentIds.isEmpty()) {
            return;
        }
        List<Card> copiedCards = new ArrayList<>();
        Map<Card, UUID> opponentsByCard = new IdentityHashMap<>();
        for (int i = 0; i < opponentIds.size(); i++) {
            for (Card sourceCard : sourceCards) {
                Card copiedCard = i == 0 ? sourceCard : sourceCard.createRuntimeCopy();
                copiedCards.add(copiedCard);
                opponentsByCard.put(copiedCard, opponentIds.get(i));
            }
        }
        BiConsumer<Card, Permanent> instructionRider = (copiedCard, token) -> opponentInstruction.accept(
                copiedCard == null ? opponentIds.getFirst() : opponentsByCard.get(copiedCard), token);
        createOrderedTokenCopies(gameData, entry, copiedCards, null, tokenControllerId, effect, null, null, null,
                tokenRider, instructionRider, false, null);
    }

    /**
     * Creates a copy of each of {@code sourceCards}, tapped and attacking, as one ordered token creation event
     * (CR 616.1). Once the replacements have fixed which tokens are created, the creating player chooses which
     * defending player or planeswalker each creature token attacks as it enters (CR 508.4), added tokens such as
     * Queen Allenal's Soldier included.
     */
    public void createTokenCopiesChoosingAttackTargets(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                       Permanent sourcePermanent, UUID tokenControllerId,
                                                       CreateTokenCopyOfTargetPermanentEffect effect) {
        createTokenCopiesChoosingAttackTargets(gameData, entry, sourceCards, sourcePermanent, tokenControllerId,
                effect, null);
    }

    /**
     * Creates the copies as {@link #createTokenCopiesChoosingAttackTargets(GameData, StackEntry, List, Permanent,
     * UUID, CreateTokenCopyOfTargetPermanentEffect)} does, setting up every token the event creates with
     * {@code tokenRider} before it enters, as in {@link #createTokenCopiesWithTokenRider}.
     */
    public void createTokenCopiesChoosingAttackTargets(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                       Permanent sourcePermanent, UUID tokenControllerId,
                                                       CreateTokenCopyOfTargetPermanentEffect effect,
                                                       Consumer<Permanent> tokenRider) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return;
        }
        createOrderedTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId, effect, null,
                null, null, tokenRider, null, true, null);
    }

    /**
     * Creates the copies as {@link #createTokenCopiesChoosingAttackTargets(GameData, StackEntry, List, Permanent,
     * UUID, CreateTokenCopyOfTargetPermanentEffect)} does, and then runs {@code afterEntry} as
     * {@link #createTokenCopiesThen} does, once every token of the event has entered with its chosen attack target.
     */
    public void createTokenCopiesChoosingAttackTargetsThen(GameData gameData, StackEntry entry,
                                                           List<Card> sourceCards, Permanent sourcePermanent,
                                                           UUID tokenControllerId,
                                                           CreateTokenCopyOfTargetPermanentEffect effect,
                                                           BiConsumer<GameData, List<Permanent>> afterEntry) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return;
        }
        createOrderedTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId, effect, null,
                null, null, null, null, true, afterEntry);
    }

    /**
     * Creates, for each of {@code opponentIds}, a copy of each of {@code sourceCards} attacking that opponent, as one
     * ordered token creation event (CR 616.1). The effect names what each copy attacks, so there is no choice
     * (CR 508.4): the copies a doubler adds attack the same opponent as the copies they double, and tokens a
     * replacement adds (Queen Allenal's Soldier) attack the first opponent. Every token the event creates is set up
     * with {@code tokenRider} before it enters, as in {@link #createTokenCopiesWithTokenRider}.
     */
    public void createTokenCopiesAttackingEachOpponent(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                       UUID tokenControllerId,
                                                       CreateTokenCopyOfTargetPermanentEffect effect,
                                                       List<UUID> opponentIds, Consumer<Permanent> tokenRider) {
        createTokenCopiesForEachOpponent(gameData, entry, sourceCards, tokenControllerId, effect, opponentIds,
                tokenRider, false);
    }

    /**
     * Creates, for each of {@code opponentIds}, a copy of each of {@code sourceCards} attacking that opponent or a
     * planeswalker they control (myriad), as one ordered token creation event (CR 616.1). Once the replacements have
     * fixed which tokens are created, the creating player chooses for each creature token between its opponent and
     * that opponent's planeswalkers as it enters (CR 508.4): the copies a doubler adds choose among the same
     * opponent's, and tokens a replacement adds (Queen Allenal's Soldier) among the first opponent's.
     */
    public void createTokenCopiesAttackingEachOpponentOrTheirPlaneswalkers(GameData gameData, StackEntry entry,
                                                                           List<Card> sourceCards,
                                                                           UUID tokenControllerId,
                                                                           CreateTokenCopyOfTargetPermanentEffect effect,
                                                                           List<UUID> opponentIds) {
        createTokenCopiesForEachOpponent(gameData, entry, sourceCards, tokenControllerId, effect, opponentIds,
                null, true);
    }

    private void createTokenCopiesForEachOpponent(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                  UUID tokenControllerId,
                                                  CreateTokenCopyOfTargetPermanentEffect effect,
                                                  List<UUID> opponentIds, Consumer<Permanent> tokenRider,
                                                  boolean chooseAttackTargets) {
        if (sourceCards == null || sourceCards.isEmpty() || opponentIds == null || opponentIds.isEmpty()) {
            return;
        }
        // The event keys copy groups and attack targets by copied card, so each further opponent's copies come from
        // a runtime copy of the card: the same copiable values, but a group of its own with its own target.
        List<Card> copiedCards = new ArrayList<>();
        Map<Card, UUID> attackTargets = new IdentityHashMap<>();
        for (int i = 0; i < opponentIds.size(); i++) {
            for (Card sourceCard : sourceCards) {
                Card copiedCard = i == 0 ? sourceCard : sourceCard.createRuntimeCopy();
                copiedCards.add(copiedCard);
                attackTargets.put(copiedCard, opponentIds.get(i));
            }
        }
        createOrderedTokenCopies(gameData, entry, copiedCards, null, tokenControllerId, effect, null,
                attackTargets, null, tokenRider, null, chooseAttackTargets, null);
    }

    private List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                         Permanent sourcePermanent, UUID tokenControllerId,
                                         CreateTokenCopyOfTargetPermanentEffect effect,
                                         List<UUID> attackTargetIds,
                                         Consumer<Card> copyException) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId,
                effect, attackTargetIds, copyException, null);
    }

    /**
     * Creates a copy of each of {@code sourceCards} attached to the permanent at the same index of {@code hosts}, as
     * one ordered token creation event (CR 616.1), and then runs {@code afterEntry} as {@link #createTokenCopiesThen}
     * does. An Aura copy enters attached to its host rather than having its controller choose what it enchants, and
     * the copies a doubler adds are attached to the same host as the copies they double; an Aura copy that couldn't
     * enchant its host isn't created (CR 303.4g). Attaching other copies, such as Equipment, is left to
     * {@code afterEntry}.
     */
    public void createTokenCopiesAttachedToThen(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                Permanent sourcePermanent, UUID tokenControllerId,
                                                CreateTokenCopyOfTargetPermanentEffect effect, List<Permanent> hosts,
                                                BiConsumer<GameData, List<Permanent>> afterEntry) {
        // The event keys copy groups by copied card, so a card copied onto a further host comes from a runtime copy
        // of it: the same copiable values, but a group of its own attached to its own host.
        List<Card> copiedCards = new ArrayList<>();
        Map<Card, UUID> hostIds = new IdentityHashMap<>();
        for (int i = 0; i < sourceCards.size(); i++) {
            Card sourceCard = sourceCards.get(i);
            Permanent host = hosts.get(i);
            Card template = copyTemplate(entry, sourceCard, effect, null);
            if (template.isAura() && !template.isEnchantZone()
                    && !auraAttachmentService.canEnchant(gameData, template, tokenControllerId, host)) {
                continue;
            }
            Card copiedCard = hostIds.containsKey(sourceCard) ? sourceCard.createRuntimeCopy() : sourceCard;
            copiedCards.add(copiedCard);
            hostIds.put(copiedCard, host.getId());
        }
        if (copiedCards.isEmpty()) {
            return;
        }
        BiConsumer<Card, Permanent> attachToHost = (copiedCard, copy) -> {
            if (copy.getCard().isAura() && !copy.getCard().isEnchantZone()) {
                copy.setAttachedTo(hostIds.get(copiedCard));
            }
        };
        createOrderedTokenCopies(gameData, entry, copiedCards, sourcePermanent, tokenControllerId, effect, null,
                null, attachToHost, null, null, false, afterEntry);
    }

    /**
     * Creates a copy of each of {@code sourceCards} as one ordered token creation event (CR 616.1). With
     * {@code attackTargetIds}, each copy enters attacking the target named for its copied card, as
     * {@link #keyCopiesByAttackTarget} maps them.
     */
    private List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                         Permanent sourcePermanent, UUID tokenControllerId,
                                         CreateTokenCopyOfTargetPermanentEffect effect,
                                         List<UUID> attackTargetIds,
                                         Consumer<Card> copyException,
                                         BiConsumer<Card, Permanent> copyRider) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return List.of();
        }
        if (attackTargetIds == null) {
            return createOrderedTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId,
                    effect, copyException, null, copyRider, null, null, false, null);
        }
        List<Card> copiedCards = new ArrayList<>();
        Map<Card, Card> originals = new IdentityHashMap<>();
        Map<Card, UUID> attackTargets = keyCopiesByAttackTarget(sourceCards, attackTargetIds, copiedCards,
                originals);
        return createOrderedTokenCopies(gameData, entry, copiedCards, sourcePermanent, tokenControllerId,
                effect, copyException, attackTargets,
                copyRider == null ? null : (copiedCard, copy) -> copyRider.accept(originals.get(copiedCard), copy),
                null, null, false, null);
    }

    /**
     * Creates a copy of each of {@code sourceCards} as one token creation event whose count replacements and
     * additions the creating player orders (CR 616.1). The copies of the first card are the event's own tokens and
     * the copies of every other card a copy group, so a doubler doubles each card's copies. The creation may pause
     * for that choice and then continue through {@link #createCopiesAfterReplacementOrder}; the ids are only
     * returned when it doesn't.
     */
    private List<UUID> createOrderedTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                                Permanent sourcePermanent, UUID tokenControllerId,
                                                CreateTokenCopyOfTargetPermanentEffect effect,
                                                Consumer<Card> copyException, Map<Card, UUID> attackTargets,
                                                BiConsumer<Card, Permanent> copyRider,
                                                Consumer<Permanent> tokenRider,
                                                BiConsumer<Card, Permanent> instructionRider,
                                                boolean chooseAttackTargets,
                                                BiConsumer<GameData, List<Permanent>> afterEntry) {
        List<Card> distinctCards = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Card sourceCard : sourceCards) {
            int index = -1;
            for (int i = 0; i < distinctCards.size(); i++) {
                if (distinctCards.get(i) == sourceCard) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                distinctCards.add(sourceCard);
                counts.add(1);
            } else {
                counts.set(index, counts.get(index) + 1);
            }
        }
        List<TokenCreationEvent.TokenGroup> copyGroups = new ArrayList<>();
        for (int i = 1; i < distinctCards.size(); i++) {
            Card template = copyTemplate(entry, distinctCards.get(i), effect, copyException);
            copyGroups.add(TokenCreationEvent.TokenGroup.copies(TokenCreationReplacementSupport.copyKind(template),
                    counts.get(i), distinctCards.get(i), TokenCreationReplacementSupport.entersAsCreature(template)));
        }
        Card sourceCard = distinctCards.getFirst();
        Card template = copyTemplate(entry, sourceCard, effect, copyException);
        TokenCreationEvent event = TokenCreationEvent.copies(tokenControllerId, counts.getFirst(), sourceCard,
                TokenCreationReplacementSupport.copyKind(template),
                TokenCreationReplacementSupport.entersAsCreature(template),
                entry.getCard() == null ? null : entry.getCard().getSetCode(),
                new TokenCopyBatch(entry, sourcePermanent, effect, copyException, attackTargets, copyRider,
                        tokenRider, instructionRider, chooseAttackTargets, afterEntry))
                .withAddedTokens(copyGroups);
        var outcome = replacementOrderService.getObject().run(gameData, event);
        if (outcome instanceof ReplacementOrderService.Outcome.Completed<TokenCreationEvent> completed) {
            return createCopiesAfterReplacementOrder(gameData, completed.event());
        }
        return List.of();
    }

    /**
     * Builds and enters the copies and the added tokens of a token-copy event whose replacements have all been
     * applied in the order the creating player chose, Jinnie Fay's and the token-kind replacements (Divine Visitation,
     * Fisher's Talent) included, so they aren't replaced again as they enter. All of them enter together. An added
     * token (Queen Allenal's Soldier, Chatterfang's Squirrels, Stridehangar Automaton's Thopter, ...) copies nothing,
     * but the creating effect's instructions apply to it, "those tokens gain haste" included (Queen Allenal, Chatterfang
     * and Stridehangar Automaton rulings); an "except it has haste" exception is part of the copy (CR 707.9a), so an
     * added token doesn't get it.
     */
    public List<UUID> createCopiesAfterReplacementOrder(GameData gameData, TokenCreationEvent event) {
        TokenCopyBatch batch = event.copyBatch();
        StackEntry entry = batch.entry();
        CreateTokenCopyOfTargetPermanentEffect effect = batch.effect();
        UUID tokenControllerId = event.controllerId();
        List<Permanent> tokens = new ArrayList<>();
        List<UUID> attackTargets = new ArrayList<>();
        List<Card> copiedCards = new ArrayList<>();
        UUID firstAttackTarget = batch.attackTargetOf(event.copyOf());
        addPreparedCopies(gameData, batch, tokenControllerId, event.copyOf(), event.tokenCount(), tokens);
        attackTargets.addAll(Collections.nCopies(tokens.size() - attackTargets.size(), firstAttackTarget));
        copiedCards.addAll(Collections.nCopies(tokens.size() - copiedCards.size(), event.copyOf()));
        for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
            if (group.substitute()) {
                addSubstitutes(gameData, batch, tokenControllerId, group, event.sourceSetCode(), tokens);
                attackTargets.addAll(Collections.nCopies(tokens.size() - attackTargets.size(),
                        group.copyOf() == null ? firstAttackTarget : batch.attackTargetOf(group.copyOf())));
                copiedCards.addAll(Collections.nCopies(tokens.size() - copiedCards.size(), group.copyOf()));
                continue;
            }
            if (group.copies()) {
                addPreparedCopies(gameData, batch, tokenControllerId, group.copyOf(), group.count(), tokens);
                attackTargets.addAll(Collections.nCopies(tokens.size() - attackTargets.size(),
                        batch.attackTargetOf(group.copyOf())));
                copiedCards.addAll(Collections.nCopies(tokens.size() - copiedCards.size(), group.copyOf()));
                continue;
            }
            CreateTokenEffect blueprint = group.blueprint();
            for (int i = 0; i < group.count(); i++) {
                Card addedCard = TokenCardFactory.create(blueprint, fixedStat(blueprint.power()),
                        fixedStat(blueprint.toughness()), event.sourceSetCode());
                tokens.add(withGrantedHaste(addedCard, addedCard, effect));
                attackTargets.add(firstAttackTarget);
                copiedCards.add(null);
            }
        }
        tokens.forEach(token -> token.setTokenKindReplacementsApplied(true));
        if (batch.tokenRider() != null) {
            tokens.forEach(batch.tokenRider());
        }
        if (batch.instructionRider() != null) {
            for (int i = 0; i < tokens.size(); i++) {
                batch.instructionRider().accept(copiedCards.get(i), tokens.get(i));
            }
        }
        boolean chooseAttackTargets = batch.chooseAttackTargets();
        if (chooseAttackTargets && batch.attackTargets() == null) {
            attackTargets = new ArrayList<>(Collections.nCopies(tokens.size(), null));
        }
        boolean explicitAttackTargets = batch.attackTargets() != null || chooseAttackTargets;
        return enterPreparedTokenCopies(gameData, entry, tokens, batch.sourcePermanent(), tokenControllerId,
                effect, attackTargets, explicitAttackTargets, chooseAttackTargets, batch.afterEntry());
    }

    /**
     * Fills {@code copiedCards} with the card each copy copies and returns the attack target of each such card's
     * copies: {@code attackTargetIds} names either one target per copied card (a {@code null} or missing entry: the
     * copy enters attacking with no target named) or a single target shared by all of them. The event keys copy
     * groups and attack targets by copied card, so the copies of one card attacking another target come from a
     * runtime copy of it: the same copiable values, but a group of its own with its own target. {@code originals}
     * maps each card put into {@code copiedCards} back to the source card it stands for.
     */
    private static Map<Card, UUID> keyCopiesByAttackTarget(List<Card> sourceCards, List<UUID> attackTargetIds,
                                                           List<Card> copiedCards, Map<Card, Card> originals) {
        boolean perCard = attackTargetIds.size() == sourceCards.size();
        boolean shared = !perCard && attackTargetIds.stream().distinct().count() == 1;
        Map<Card, UUID> attackTargets = new IdentityHashMap<>();
        for (int i = 0; i < sourceCards.size(); i++) {
            Card sourceCard = sourceCards.get(i);
            UUID attackTarget = shared ? attackTargetIds.getFirst()
                    : i < attackTargetIds.size() ? attackTargetIds.get(i) : null;
            Card copiedCard = null;
            boolean sourceCardKeyed = false;
            for (Card keyed : attackTargets.keySet()) {
                if (originals.get(keyed) == sourceCard) {
                    sourceCardKeyed = true;
                    if (Objects.equals(attackTargets.get(keyed), attackTarget)) {
                        copiedCard = keyed;
                        break;
                    }
                }
            }
            if (copiedCard == null) {
                copiedCard = sourceCardKeyed ? sourceCard.createRuntimeCopy() : sourceCard;
                attackTargets.put(copiedCard, attackTarget);
                originals.put(copiedCard, sourceCard);
            }
            copiedCards.add(copiedCard);
        }
        return attackTargets;
    }

    private Card copyTemplate(StackEntry entry, Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect,
                              Consumer<Card> copyException) {
        Card template = buildTokenCopyCard(
                sourceCard, effect, gameQueryService::isCreatureSubtype, entry.getCard(), true);
        if (copyException != null) {
            copyException.accept(template);
        }
        return template;
    }

    /**
     * A copy exception for "except it's a [creature type]" without "in addition to its other types": replaces the
     * copy's creature types with {@code creatureTypes}, keeping its other subtypes; a changeling copy loses
     * changeling, since the exception defines its creature types (CR 707.9d). The copy's back face, built by the same
     * copy (CR 707.8a), gets the exception too; a prepare spell back face ignores it. Does nothing for no creature
     * types.
     */
    public void replaceCreatureTypes(Card tokenCard, List<CardSubtype> creatureTypes) {
        if (creatureTypes == null || creatureTypes.isEmpty()) {
            return;
        }
        Card backFace = tokenCard.getBackFaceCard();
        if (backFace != null && !tokenCard.isModalDoubleFaced()
                && !backFace.hasType(CardType.INSTANT) && !backFace.hasType(CardType.SORCERY)) {
            replaceCreatureTypes(backFace, creatureTypes);
        }
        List<CardSubtype> subtypes = new ArrayList<>();
        if (tokenCard.getSubtypes() != null) {
            for (CardSubtype subtype : tokenCard.getSubtypes()) {
                if (!gameQueryService.isCreatureSubtype(subtype)) {
                    subtypes.add(subtype);
                }
            }
        }
        for (CardSubtype creatureType : creatureTypes) {
            if (!subtypes.contains(creatureType)) {
                subtypes.add(creatureType);
            }
        }
        tokenCard.setSubtypes(subtypes);
        if (tokenCard.getKeywords() != null && tokenCard.getKeywords().contains(Keyword.CHANGELING)) {
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(tokenCard.getKeywords());
            keywords.remove(Keyword.CHANGELING);
            tokenCard.setKeywords(keywords);
        }
    }

    /** Builds {@code count} copies of {@code sourceCard} with the batch's exceptions and riders into {@code tokens}. */
    private void addPreparedCopies(GameData gameData, TokenCopyBatch batch, UUID tokenControllerId, Card sourceCard,
                                   int count, List<Permanent> tokens) {
        CreateTokenCopyOfTargetPermanentEffect effect = batch.effect();
        for (int copy = 0; copy < count; copy++) {
            Card tokenCard = copyTemplate(batch.entry(), sourceCard, effect, batch.copyException());
            Permanent preparedToken = withGrantedDeathEffects(withGrantedHaste(tokenCard, sourceCard, effect), effect);
            if (tokenCard.hasType(CardType.PLANESWALKER) && tokenCard.getLoyalty() != null) {
                preparedToken.setCounterCount(CounterType.LOYALTY, tokenCard.getLoyalty());
            }
            if (batch.copyRider() != null) {
                batch.copyRider().accept(sourceCard, preparedToken);
            }
            tokens.add(preparedToken);
        }
    }

    /**
     * Builds the {@code group.count()} Cats or Dogs Jinnie Fay substituted for tokens of the batch into
     * {@code tokens}. They aren't copies, so the copy rider doesn't apply, but they get the creating effect's riders
     * (haste, granted death trigger), since anything else the creating effect specifies still applies to her tokens
     * (Jinnie Fay ruling, 2022-04-29).
     */
    private static void addSubstitutes(GameData gameData, TokenCopyBatch batch, UUID tokenControllerId,
                                       TokenCreationEvent.TokenGroup group, String setCode, List<Permanent> tokens) {
        CreateTokenEffect blueprint = group.blueprint();
        for (int i = 0; i < group.count(); i++) {
            Card substitute = TokenCardFactory.create(blueprint, fixedStat(blueprint.power()),
                    fixedStat(blueprint.toughness()), setCode);
            tokens.add(withGrantedDeathEffects(withGrantedHaste(substitute, substitute, batch.effect()),
                    batch.effect()));
        }
    }

    private static int fixedStat(DynamicAmount stat) {
        return stat instanceof Fixed fixed ? fixed.value() : 0;
    }

    private List<UUID> enterPreparedTokenCopies(GameData gameData, StackEntry entry, List<Permanent> tokens,
                                                Permanent sourcePermanent, UUID tokenControllerId,
                                                CreateTokenCopyOfTargetPermanentEffect effect,
                                                List<UUID> expandedAttackTargets, boolean explicitAttackTargets,
                                                boolean chooseOpponentAttackTargets,
                                                BiConsumer<GameData, List<Permanent>> afterEntry) {
        if (chooseOpponentAttackTargets) {
            continueOpponentAttackTargetChoices(gameData,
                    new PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking(
                            tokenControllerId, entry, effect, tokens, expandedAttackTargets, List.of(), afterEntry));
            return List.of();
        }
        return putPreparedTokenCopiesOntoBattlefield(gameData, entry, tokens, sourcePermanent,
                tokenControllerId, effect, expandedAttackTargets, explicitAttackTargets, afterEntry);
    }

    /** Continues a restricted player-or-planeswalker choice for each prepared myriad token. */
    public void completeOpponentAttackTargetChoice(GameData gameData, UUID chosenTarget,
                                                   PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking context) {
        int index = context.chosenAttackTargets().size();
        if (index >= context.tokenOpponents().size()) return;
        UUID opponentId = context.tokenOpponents().get(index);
        boolean legal = attackTargetOptions(gameData, context.controllerId(), opponentId).contains(chosenTarget);
        if (!legal) throw new IllegalStateException("The token must attack that opponent or a planeswalker they control");
        List<UUID> chosenTargets = new ArrayList<>(context.chosenAttackTargets());
        chosenTargets.add(chosenTarget);
        continueOpponentAttackTargetChoices(gameData,
                new PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking(
                        context.controllerId(), context.entry(), context.copyEffect(), context.tokens(),
                        context.tokenOpponents(), chosenTargets, context.afterEntry()));
    }

    private void continueOpponentAttackTargetChoices(GameData gameData,
                                                      PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking context) {
        List<UUID> chosenTargets = new ArrayList<>(context.chosenAttackTargets());
        while (chosenTargets.size() < context.tokenOpponents().size()) {
            int index = chosenTargets.size();
            UUID opponentId = context.tokenOpponents().get(index);
            if (!context.tokens().get(index).getCard().hasType(CardType.CREATURE)) {
                chosenTargets.add(null);
                continue;
            }
            List<UUID> playerIds = attackablePlayers(gameData, context.controllerId(), opponentId);
            List<UUID> permanentIds = attackablePermanents(gameData, playerIds, opponentId == null);
            if (permanentIds.size() + playerIds.size() <= 1) {
                chosenTargets.add(playerIds.isEmpty()
                        ? (permanentIds.isEmpty() ? null : permanentIds.getFirst()) : playerIds.getFirst());
                continue;
            }
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking(
                            context.controllerId(), context.entry(), context.copyEffect(), context.tokens(),
                            context.tokenOpponents(), chosenTargets, context.afterEntry()));
            playerInputService.beginAnyTargetChoice(gameData, context.controllerId(), permanentIds, playerIds,
                    opponentId == null
                            ? "Choose the player, planeswalker, or battle for the token to attack."
                            : "Choose the player or planeswalker for the token to attack.");
            return;
        }
        UUID firstChosenTarget = chosenTargets.stream().filter(Objects::nonNull).findFirst().orElse(null);
        chosenTargets.replaceAll(target -> target == null ? firstChosenTarget : target);
        putPreparedTokenCopiesOntoBattlefield(gameData, context.entry(), context.tokens(), null,
                context.controllerId(), context.copyEffect(), chosenTargets, true, context.afterEntry());
    }

    /**
     * What a prepared token may attack: {@code opponentId} or a planeswalker they control (myriad and "attacking
     * that opponent" name the player, so no battle), or, when {@code opponentId} is {@code null}, any defending
     * player, planeswalker a defending player controls or battle a defending player protects (CR 508.4).
     */
    private List<UUID> attackTargetOptions(GameData gameData, UUID controllerId, UUID opponentId) {
        List<UUID> playerIds = attackablePlayers(gameData, controllerId, opponentId);
        List<UUID> options = new ArrayList<>(playerIds);
        options.addAll(attackablePermanents(gameData, playerIds, opponentId == null));
        return options;
    }

    private static List<UUID> attackablePlayers(GameData gameData, UUID controllerId, UUID opponentId) {
        return opponentId != null ? List.of(opponentId)
                : gameData.orderedPlayerIds.stream().filter(playerId -> !playerId.equals(controllerId)).toList();
    }

    private List<UUID> attackablePermanents(GameData gameData, List<UUID> playerIds, boolean includeBattles) {
        List<UUID> permanentIds = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if ((gameQueryService.isPlaneswalker(gameData, permanent) && playerIds.contains(controllerId))
                    || (includeBattles && gameQueryService.isBattle(gameData, permanent)
                    && playerIds.contains(permanent.getProtectorPlayerId()))) {
                permanentIds.add(permanent.getId());
            }
        });
        return permanentIds;
    }

    /**
     * Granted "when this token dies" abilities (CR 707.2: abilities added by an effect are not part of
     * the copiable values) are stored on the token permanent rather than its copiable card, so a copy
     * of the token does not inherit them.
     */
    private static Permanent withGrantedDeathEffects(Permanent token, CreateTokenCopyOfTargetPermanentEffect effect) {
        if (effect.additionalSlotEffects() != null) {
            effect.additionalSlotEffects().getOrDefault(EffectSlot.ON_DEATH, List.of())
                    .forEach(death -> token.addPersistentTriggeredEffect(EffectSlot.ON_DEATH, death));
        }
        return token;
    }

    /**
     * "It gains haste" is an effect granting the token haste, not an exception to the copy
     * (CR 707.2), so it is moved off the copiable token card onto the permanent where a copy of the
     * token won't pick it up. A frozen token card is replaced by a mutable runtime copy first.
     */
    private static Permanent withGrantedHaste(Card tokenCard, Card sourceCard,
                                              CreateTokenCopyOfTargetPermanentEffect effect) {
        if (!effect.grantHaste()) {
            return new Permanent(tokenCard);
        }
        boolean copiedHaste = sourceCard.hasKeyword(Keyword.HASTE)
                || (effect.additionalKeywords() != null && effect.additionalKeywords().contains(Keyword.HASTE));
        Card copiableCard = tokenCard;
        if (!copiedHaste && tokenCard.getKeywords() != null && tokenCard.getKeywords().contains(Keyword.HASTE)) {
            Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(tokenCard.getKeywords());
            keywords.remove(Keyword.HASTE);
            copiableCard = tokenCard.createRuntimeCopy();
            copiableCard.setKeywords(keywords);
        }
        Permanent token = new Permanent(copiableCard);
        if (!effect.hasteUntilEndOfTurn()) {
            token.getPersistentGrantedKeywords().add(Keyword.HASTE);
        }
        return token;
    }

    private List<UUID> putPreparedTokenCopiesOntoBattlefield(GameData gameData, StackEntry entry,
                                                            List<Permanent> tokens, Permanent sourcePermanent,
                                                            UUID tokenControllerId,
                                                            CreateTokenCopyOfTargetPermanentEffect effect,
                                                            List<UUID> expandedAttackTargets,
                                                            boolean explicitAttackTargets,
                                                            BiConsumer<GameData, List<Permanent>> afterEntry) {
        for (int i = 0; i < tokens.size(); i++) {
            Permanent token = tokens.get(i);
            if (!token.getCard().isAura() || token.getCard().isEnchantZone() || token.isAttached()) continue;
            List<UUID> hosts = new ArrayList<>();
            gameData.forEachPermanent((controller, permanent) -> {
                if (!gameQueryService.cantBeEnchantedByOtherAuras(gameData, permanent)
                        && auraAttachmentService.canEnchant(gameData, token.getCard(), tokenControllerId, permanent)) {
                    hosts.add(permanent.getId());
                }
            });
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (auraAttachmentService.canEnchantPlayer(gameData, token.getCard(), tokenControllerId, playerId)) {
                    hosts.add(playerId);
                }
            }
            if (hosts.isEmpty()) continue;
            if (hosts.size() == 1) {
                token.setAttachedTo(hosts.getFirst());
            } else {
                playerInputService.beginPermanentChoice(gameData, tokenControllerId, hosts,
                        new PermanentChoiceContext.PreparedTokenCopyAttachments(tokenControllerId, entry, effect,
                                tokens, sourcePermanent, expandedAttackTargets, explicitAttackTargets, i,
                                afterEntry),
                        "Choose what " + token.getCard().getName() + " will enchant.");
                return List.of();
            }
        }
        tokens = tokens.stream().filter(token -> !token.getCard().isAura()
                || token.getCard().isEnchantZone() || token.isAttached()).toList();
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = tokens;
        List<UUID> createdIds = new ArrayList<>();
        int tokenIndex = 0;
        for (Permanent tokenPermanent : tokens) {
            // "Enters with an additional counter" (CR 122.6): the counters are there as it enters, so
            // entry triggers that read power or counters see them.
            if (effect.initialCounters() != null && !effect.initialCounters().isEmpty()
                    && !gameQueryService.cantHaveCounters(gameData, tokenPermanent)) {
                for (var counterEntry : effect.initialCounters().entrySet()) {
                    if (counterEntry.getValue() > 0) {
                        int counters = gameQueryService.replaceCounters(gameData, tokenPermanent,
                                tokenControllerId, counterEntry.getKey(), counterEntry.getValue(),
                                tokenControllerId);
                        tokenPermanent.setCounterCount(counterEntry.getKey(),
                                tokenPermanent.getCounterCount(counterEntry.getKey()) + counters);
                    }
                }
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, tokenControllerId, tokenPermanent, enterTappedTypes, simultaneouslyEntered,
                entry.getTokenCopyXValue(), false);
            entry.getCreatedPermanentIds().add(tokenPermanent.getId());
            createdIds.add(tokenPermanent.getId());
            if (effect.trackWithSource() && entry.getSourcePermanentId() != null) {
                gameData.sourceCreatedTokens
                        .computeIfAbsent(entry.getSourcePermanentId(), ignored -> ConcurrentHashMap.newKeySet())
                        .add(tokenPermanent.getId());
            }

            if (effect.tapped() || effect.tappedAndAttacking()) {
                tokenPermanent.tap();
            }
            if (effect.tappedAndAttacking() && gameQueryService.isCreature(gameData, tokenPermanent)) {
                tokenPermanent.enterAttacking(true);
                if (explicitAttackTargets && tokenIndex < expandedAttackTargets.size()) {
                    tokenPermanent.setAttackTarget(expandedAttackTargets.get(tokenIndex));
                } else if (!explicitAttackTargets && sourcePermanent != null) {
                    tokenPermanent.setAttackTarget(sourcePermanent.getAttackTarget());
                }
            }
            tokenIndex++;

            if (effect.exileAtEndStep()) {
                // CR 603.7e: the ability that created the delayed exile keeps controlling it, even if the
                // token changes control before the end step.
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP,
                        false, null, null, entry.getControllerId(), false, entry.getCard(),
                        entry.getControllerId(), entry.getSourcePermanentId()));
            }
            if (effect.exileAtEndOfCombat()) {
                // One resolution's tokens share a single "exile them at end of combat" delayed trigger;
                // the source card is the batching key. CR 603.7e: the ability that created the delayed
                // trigger keeps controlling it and remains its source, even if a token changes control.
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT,
                        false, null, null, null, false, entry.getCard(),
                        entry.getControllerId(), entry.getSourcePermanentId()));
            }
            if (effect.sacrificeAtEndStep()) {
                // Only the player who created the token sacrifices it, and only while they control it
                // The source card keys the batch: several tokens from one resolution share a single
                // "sacrifice those tokens at the beginning of the next end step" delayed trigger.
                gameData.queueDelayedAction(tokens.size() > 1
                        ? new DelayedPermanentAction(
                                tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP,
                                false, null, null, tokenControllerId, false, entry.getCard())
                        : new DelayedPermanentAction(
                                tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP,
                                false, null, null, tokenControllerId));
            }
            if (effect.sacrificeAtNextUpkeep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_NEXT_UPKEEP));
            }
            if (effect.grantHaste() && effect.hasteUntilEndOfTurn()) {
                tokenPermanent.getGrantedKeywords().add(Keyword.HASTE);
                gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                        entry.getCard().getName(), null, entry.getControllerId(),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        tokenPermanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            }

            Card sourceCard = tokenPermanent.getCard();
            gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
        }
        List<Permanent> enteredTokens = tokens;
        Runnable entryNotifications = () -> {
            for (Permanent tokenPermanent : enteredTokens) {
                Card sourceCard = tokenPermanent.getCard();
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, tokenControllerId, sourceCard, null, false);
                if (sourceCard.isSaga()) {
                    sagaChapterService.initializeSaga(gameData, tokenPermanent, sourceCard, tokenControllerId);
                }
            }
            battlefieldEntryService.checkAllyTokenEntersTriggers(
                    gameData, tokenControllerId, enteredTokens.stream().map(Permanent::getId).toList());
        };
        if (gameData.deferredTokenEntryNotifications != null) {
            gameData.deferredTokenEntryNotifications.add(entryNotifications);
        } else {
            entryNotifications.run();
        }
        animateTokensThatAreNotCreatures(gameData, entry, tokens, tokenControllerId, effect);
        if (afterEntry != null) {
            afterEntry.accept(gameData, tokens);
        }
        return createdIds;
    }

    /**
     * Applies the effect's post-entry animation to each token that is not a creature. The tokens have
     * already entered as plain copies, so entry triggers did not see the animation's types.
     */
    private void animateTokensThatAreNotCreatures(GameData gameData, StackEntry entry, List<Permanent> tokens,
                                                  UUID tokenControllerId,
                                                  CreateTokenCopyOfTargetPermanentEffect effect) {
        AnimatePermanentsEffect animation = effect.animateIfNotCreature();
        if (animation == null) {
            return;
        }
        int power = ((Fixed) animation.power()).value();
        int toughness = ((Fixed) animation.toughness()).value();
        String sourceName = entry.getCard() == null ? effect.nameOverride() : entry.getCard().getName();
        for (Permanent token : tokens) {
            if (gameQueryService.findPermanentById(gameData, token.getId()) == null
                    || gameQueryService.isCreature(gameData, token)) {
                continue;
            }
            animationSupport.getObject().animatePermanently(gameData, token, animation, power, toughness,
                    sourceName, entry.getSourcePermanentId(), entry.getControllerId());
        }
    }

    /** Resumes the token-copy batch after one legal, untargeted Aura attachment choice. */
    public void completeTokenCopyAttachmentChoice(GameData gameData, UUID attachmentId,
            PermanentChoiceContext.PreparedTokenCopyAttachments context) {
        context.tokens().get(context.choosingIndex()).setAttachedTo(attachmentId);
        putPreparedTokenCopiesOntoBattlefield(gameData, context.entry(), context.tokens(),
                context.sourcePermanent(), context.controllerId(), context.copyEffect(),
                context.attackTargets(), context.explicitAttackTargets(), context.afterEntry());
    }

    static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect) {
        return buildTokenCopyCard(sourceCard, effect, null);
    }

    private static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect,
                                           Predicate<CardSubtype> isCreatureSubtype) {
        return buildTokenCopyCard(sourceCard, effect, isCreatureSubtype, null);
    }

    private static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect,
                                           Predicate<CardSubtype> isCreatureSubtype, Card targetingSourceCard) {
        return buildTokenCopyCard(sourceCard, effect, isCreatureSubtype, targetingSourceCard, false);
    }

    /**
     * @param deathGrantsOnPermanent when true, granted ON_DEATH effects are left off the returned card;
     *                               the caller attaches them to the token permanent instead
     */
    private static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect,
                                           Predicate<CardSubtype> isCreatureSubtype, Card targetingSourceCard,
                                           boolean deathGrantsOnPermanent) {
        boolean hasPTOverride = effect.powerOverride() != null || effect.toughnessOverride() != null;

        Card tokenCard = new Card();
        tokenCard.setName(effect.nameOverride() != null ? effect.nameOverride() : sourceCard.getName());
        boolean creatureSubtypesReplaced = effect.creatureSubtypeOverride() != null
                && !effect.creatureSubtypeOverride().isEmpty();
        if (creatureSubtypesReplaced) {
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setAdditionalTypes(EnumSet.noneOf(CardType.class));
        } else {
            tokenCard.setType(sourceCard.getType());
            tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
        }
        tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
        tokenCard.setToken(true);
        if (sourceCard.getBackFaceCard() != null && !sourceCard.isModalDoubleFaced()) {
            Card backFace = sourceCard.getBackFaceCard();
            if (backFace.hasType(CardType.INSTANT) || backFace.hasType(CardType.SORCERY)) {
                // A prepare spell stays part of the copy and ignores the copy exceptions (CR 722.3c).
                tokenCard.setBackFaceCard(backFace);
            } else {
                tokenCard.setBackFaceCard(buildTokenCopyCard(backFace, effect,
                        isCreatureSubtype, targetingSourceCard, deathGrantsOnPermanent));
            }
        }
        CardColor color = effect.colorOverride() != null ? effect.colorOverride() : sourceCard.getColor();
        tokenCard.setColor(color);
        List<CardColor> colors = effect.colorOverride() != null
                ? new ArrayList<>(List.of(effect.colorOverride()))
                : sourceCard.getColors() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(sourceCard.getColors());
        if (effect.additionalColors() != null) {
            for (CardColor additionalColor : effect.additionalColors()) {
                if (!colors.contains(additionalColor)) {
                    colors.add(additionalColor);
                }
            }
        }
        tokenCard.setColors(colors);
        EnumSet<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        if (sourceCard.getSupertypes() != null) {
            supertypes.addAll(sourceCard.getSupertypes());
        }
        if (effect.removeLegendary()) {
            supertypes.remove(CardSupertype.LEGENDARY);
        }
        if (effect.additionalSupertypes() != null) {
            supertypes.addAll(effect.additionalSupertypes());
        }
        tokenCard.setSupertypes(supertypes);
        tokenCard.setPower(effect.powerOverride() != null ? effect.powerOverride() : sourceCard.getPower());
        tokenCard.setToughness(effect.toughnessOverride() != null ? effect.toughnessOverride() : sourceCard.getToughness());
        tokenCard.setLoyalty(sourceCard.getLoyalty());
        tokenCard.setAttachRestriction(sourceCard.getAttachRestriction());
        tokenCard.setCardText(sourceCard.getCardText());
        tokenCard.setSetCode(sourceCard.getSetCode());
        tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

        List<CardSubtype> subtypes = new ArrayList<>();
        if (effect.creatureSubtypeOverride() != null && !effect.creatureSubtypeOverride().isEmpty()) {
            if (isCreatureSubtype == null) {
                throw new IllegalStateException("Creature subtype override requires subtype classification");
            }
            if (sourceCard.getSubtypes() != null) {
                for (CardSubtype subtype : sourceCard.getSubtypes()) {
                    if (!isCreatureSubtype.test(subtype)) {
                        subtypes.add(subtype);
                    }
                }
            }
            for (CardSubtype subtype : effect.creatureSubtypeOverride()) {
                if (!subtypes.contains(subtype)) {
                    subtypes.add(subtype);
                }
            }
        } else {
            if (sourceCard.getSubtypes() != null) {
                subtypes.addAll(sourceCard.getSubtypes());
            }
            if (effect.additionalSubtypes() != null) {
                // CR 205.3d: a creature subtype can't be gained by an object that is not a creature
                // (or kindred), e.g. a copied land.
                boolean canGainCreatureSubtypes = isCreatureSubtype == null
                        || sourceCard.hasType(CardType.CREATURE) || sourceCard.hasType(CardType.KINDRED)
                        || (effect.additionalTypes() != null
                        && (effect.additionalTypes().contains(CardType.CREATURE)
                        || effect.additionalTypes().contains(CardType.KINDRED)));
                for (CardSubtype subtype : effect.additionalSubtypes()) {
                    if (!subtypes.contains(subtype)
                            && (canGainCreatureSubtypes || !isCreatureSubtype.test(subtype))) {
                        subtypes.add(subtype);
                    }
                }
            }
        }
        tokenCard.setSubtypes(subtypes);

        if (effect.replaceCardTypes() && effect.additionalTypes() != null && !effect.additionalTypes().isEmpty()) {
            Set<CardType> replacement = EnumSet.noneOf(CardType.class);
            replacement.addAll(effect.additionalTypes());
            CardType primaryType = replacement.iterator().next();
            replacement.remove(primaryType);
            tokenCard.setType(primaryType);
            tokenCard.setAdditionalTypes(replacement);
        } else if (effect.additionalTypes() != null && !effect.additionalTypes().isEmpty()) {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(tokenCard.getAdditionalTypes());
            for (CardType additionalType : effect.additionalTypes()) {
                if (additionalType != tokenCard.getType() && !merged.contains(additionalType)) {
                    merged.add(additionalType);
                }
            }
            tokenCard.setAdditionalTypes(merged);
        }

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (sourceCard.getKeywords() != null) {
            keywords.addAll(sourceCard.getKeywords());
        }
        if (effect.grantHaste()) {
            keywords.add(Keyword.HASTE);
        }
        if (effect.additionalKeywords() != null) {
            keywords.addAll(effect.additionalKeywords());
        }
        if (creatureSubtypesReplaced) {
            keywords.remove(Keyword.CHANGELING);
        }
        if (!keywords.isEmpty()) {
            tokenCard.setKeywords(keywords);
        }

        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration registration : sourceCard.getEffectRegistrations(slot)) {
                if (hasPTOverride && registration.effect().isPowerToughnessDefining()) {
                    continue;
                }
                tokenCard.addEffect(slot, registration.effect(), registration.triggerMode());
            }
        }
        if (effect.additionalSlotEffects() != null) {
            effect.additionalSlotEffects().forEach((slot, effects) -> {
                if (deathGrantsOnPermanent && slot == EffectSlot.ON_DEATH) {
                    return;
                }
                effects.forEach(additionalEffect -> tokenCard.addEffect(slot, additionalEffect,
                        com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT));
            });
        }
        for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
            tokenCard.addActivatedAbility(ability);
        }
        tokenCard.copyTargetingFrom(sourceCard);
        if (targetingSourceCard != null && effect.additionalSlotEffects() != null) {
            List<CardEffect> additionalEffects =
                    effect.additionalSlotEffects().values().stream().flatMap(List::stream).toList();
            tokenCard.appendSpellTargetingForEffectsFrom(targetingSourceCard, additionalEffects);
        }
        return tokenCard;
    }
}
