package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Shared token-copy creation used by target-copy effects and last-known-information riders. */
@Component
@RequiredArgsConstructor
public class TokenCopySupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final SagaChapterService sagaChapterService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.aura.AuraAttachmentService auraAttachmentService;

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

    private List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                         Permanent sourcePermanent, UUID tokenControllerId,
                                         CreateTokenCopyOfTargetPermanentEffect effect,
                                         List<UUID> attackTargetIds,
                                         Consumer<Card> copyException) {
        return createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId,
                effect, attackTargetIds, copyException, false);
    }

    /** Prepares all per-opponent copies, including token replacements, before choosing their attack targets. */
    public void createTokenCopiesChoosingOpponentAttackTargets(GameData gameData, StackEntry entry,
                                                               List<Card> sourceCards, Permanent sourcePermanent,
                                                               UUID tokenControllerId,
                                                               CreateTokenCopyOfTargetPermanentEffect effect,
                                                               List<UUID> opponentIds) {
        createTokenCopies(gameData, entry, sourceCards, sourcePermanent, tokenControllerId, effect,
                opponentIds, null, true);
    }

    private List<UUID> createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                         Permanent sourcePermanent, UUID tokenControllerId,
                                         CreateTokenCopyOfTargetPermanentEffect effect,
                                         List<UUID> attackTargetIds,
                                         Consumer<Card> copyException, boolean chooseOpponentAttackTargets) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return List.of();
        }

        List<Permanent> tokens = new ArrayList<>();
        List<UUID> expandedAttackTargets = new ArrayList<>();
        int sourceCardIndex = 0;
        Card artifactTokenTemplate = null;
        CreateTokenEffect manufactorOriginal = null;
        int manufactorAmount = 0;
        for (Card sourceCard : sourceCards) {
            UUID sourceAttackTarget = attackTargetIds != null && sourceCardIndex < attackTargetIds.size()
                    ? attackTargetIds.get(sourceCardIndex) : null;
            sourceCardIndex++;
            Card tokenTemplate = buildTokenCopyCard(
                    sourceCard, effect, gameQueryService::isCreatureSubtype, entry.getCard());
            if (copyException != null) {
                copyException.accept(tokenTemplate);
            }
            int tokenMultiplier = gameQueryService.getTokenCreationAmount(
                    gameData, tokenControllerId, 1, tokenTemplate.getSubtypes(), tokenTemplate.hasType(CardType.CREATURE));
            CreateTokenEffect replacementOriginal = CreateTokenEffect.ofArtifactToken(
                    1, tokenTemplate.getName(), tokenTemplate.getSubtypes(), List.of());
            if (!TokenCreationReplacementSupport.academyManufactorTokenBlueprints(
                    gameData, tokenControllerId, replacementOriginal, 1).isEmpty()) {
                if (artifactTokenTemplate == null && tokenTemplate.hasType(CardType.ARTIFACT)) {
                    artifactTokenTemplate = tokenTemplate;
                }
                manufactorOriginal = replacementOriginal;
                manufactorAmount += tokenMultiplier;
                continue;
            }
            for (int copy = 0; copy < tokenMultiplier; copy++) {
                Card tokenCard = copy == 0
                        ? tokenTemplate
                        : buildTokenCopyCard(
                                sourceCard, effect, gameQueryService::isCreatureSubtype, entry.getCard());
                if (copy != 0 && copyException != null) {
                    copyException.accept(tokenCard);
                }
                if (artifactTokenTemplate == null && tokenCard.hasType(CardType.ARTIFACT)) {
                    artifactTokenTemplate = tokenCard;
                }
                tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                        gameData, tokenControllerId, tokenCard);
                tokens.add(withGrantedHaste(tokenCard, sourceCard, effect));
                expandedAttackTargets.add(sourceAttackTarget);
            }
        }
        if (manufactorOriginal != null) {
            for (CreateTokenEffect blueprint : TokenCreationReplacementSupport.academyManufactorTokenBlueprints(
                    gameData, tokenControllerId, manufactorOriginal, manufactorAmount)) {
                tokens.add(new Permanent(TokenCardFactory.create(blueprint, 0, 0,
                        entry.getCard() == null ? null : entry.getCard().getSetCode())));
            }
        }
        boolean creatureTokenEvent = tokens.stream()
                .anyMatch(token -> token.getCard().hasType(CardType.CREATURE));
        int additionalSoldierTokenCount = TokenCreationReplacementSupport.additionalSoldierTokenCountIfApplicable(
                gameData, tokenControllerId, creatureTokenEvent);
        CreateTokenEffect additionalSoldier = additionalSoldierTokenCount > 0
                ? TokenCreationReplacementSupport.additionalSoldierTokenIfApplicable(
                        gameData, tokenControllerId, creatureTokenEvent,
                        effect.tappedAndAttacking(), effect.tapped())
                : null;
        for (int i = 0; i < additionalSoldierTokenCount; i++) {
            Card soldierTokenCard = TokenCardFactory.create(additionalSoldier, 1, 1,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            soldierTokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                    gameData, tokenControllerId, soldierTokenCard);
            tokens.add(new Permanent(soldierTokenCard));
        }
        int additionalMapTokenCount = TokenCreationReplacementSupport.additionalMapTokenCount(
                gameData, tokenControllerId, artifactTokenTemplate, 1);
        for (int map = 0; map < additionalMapTokenCount; map++) {
            Card mapTokenCard = TokenCardFactory.create(
                    TokenCreationReplacementSupport.additionalMapToken(
                            effect.tapped(), effect.tappedAndAttacking()),
                    0,
                    0,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            tokens.add(new Permanent(mapTokenCard));
        }
        int additionalMutagenTokenCount = TokenCreationReplacementSupport.additionalMutagenTokenCount(
                gameData, tokenControllerId, sourceCards.size());
        for (int mutagen = 0; mutagen < additionalMutagenTokenCount; mutagen++) {
            Card mutagenTokenCard = TokenCardFactory.create(
                    TokenCreationReplacementSupport.additionalMutagenToken(effect.tapped(), effect.tappedAndAttacking()),
                    0,
                    0,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            tokens.add(new Permanent(mutagenTokenCard));
        }
        int additionalFoodTokenCount = TokenCreationReplacementSupport.additionalFoodTokenCount(
                gameData, tokenControllerId, sourceCards.size());
        for (int food = 0; food < additionalFoodTokenCount; food++) {
            Card foodTokenCard = TokenCardFactory.create(
                    TokenCreationReplacementSupport.additionalFoodToken(effect.tapped(), effect.tappedAndAttacking()),
                    0,
                    0,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            tokens.add(new Permanent(foodTokenCard));
        }

        int additionalClueCount = tokens.isEmpty() ? 0
                : permanentControlSupport.solvedClueReplacementCount(gameData, tokenControllerId);
        CreateTokenEffect clue = CreateTokenEffect.ofClueToken(1);
        for (int i = 0; i < additionalClueCount; i++) {
            int clueAmount = gameQueryService.getTokenCreationAmount(gameData, tokenControllerId,
                    1, clue.subtypes(), false);
            List<CreateTokenEffect> clueBlueprints = TokenCreationReplacementSupport.academyManufactorTokenBlueprints(
                    gameData, tokenControllerId, clue, clueAmount);
            if (clueBlueprints.isEmpty()) {
                for (int j = 0; j < clueAmount; j++) {
                    tokens.add(new Permanent(TokenCardFactory.create(clue, 0, 0,
                            entry.getCard() == null ? null : entry.getCard().getSetCode())));
                }
            } else {
                for (CreateTokenEffect blueprint : clueBlueprints) {
                    tokens.add(new Permanent(TokenCardFactory.create(blueprint, 0, 0,
                            entry.getCard() == null ? null : entry.getCard().getSetCode())));
                }
            }
        }

        int additionalTreasureTokenCount = tokens.stream().mapToInt(token ->
                TokenCreationReplacementSupport.additionalTreasureTokenCount(
                        gameData, tokenControllerId, token.getCard(), 1)).sum();
        for (int treasure = 0; treasure < additionalTreasureTokenCount; treasure++) {
            Card treasureToken = TokenCardFactory.create(CreateTokenEffect.ofTreasureToken(1), 0, 0,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            tokens.add(new Permanent(treasureToken));
        }

        int squirrelCount = TokenCreationReplacementSupport.additionalSquirrelTokenCount(
                gameData, tokenControllerId, tokens.size());
        CreateTokenEffect squirrel = squirrelCount == 0 ? null
                : TokenCreationReplacementSupport.additionalSquirrelTokenIfApplicable(
                        gameData, tokenControllerId,
                        new CreateTokenEffect(1, "Squirrel", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));
        if (!creatureTokenEvent && squirrelCount > 0) {
            squirrelCount = gameQueryService.getNewCreatureTokenCreationAmount(
                    gameData, tokenControllerId, squirrelCount, squirrel.subtypes());
        }
        int originalAttackTargetCount = expandedAttackTargets.size();
        for (int i = 0; i < squirrelCount; i++) {
            Card squirrelCard = TokenCardFactory.create(squirrel, 1, 1,
                    entry.getCard() == null ? null : entry.getCard().getSetCode());
            squirrelCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                    gameData, tokenControllerId, squirrelCard);
            tokens.add(withGrantedHaste(squirrelCard, squirrelCard, effect));
            expandedAttackTargets.add(originalAttackTargetCount == 0 ? null
                    : expandedAttackTargets.get(i % originalAttackTargetCount));
        }

        if (chooseOpponentAttackTargets) {
            continueOpponentAttackTargetChoices(gameData,
                    new PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking(
                            tokenControllerId, entry, effect, tokens, expandedAttackTargets, List.of()));
            return List.of();
        }
        return putPreparedTokenCopiesOntoBattlefield(gameData, entry, tokens, sourcePermanent,
                tokenControllerId, effect, expandedAttackTargets, attackTargetIds != null);
    }

    /** Continues a restricted player-or-planeswalker choice for each prepared myriad token. */
    public void completeOpponentAttackTargetChoice(GameData gameData, UUID chosenTarget,
                                                   PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking context) {
        int index = context.chosenAttackTargets().size();
        if (index >= context.tokenOpponents().size()) return;
        UUID opponentId = context.tokenOpponents().get(index);
        boolean legal = opponentId.equals(chosenTarget)
                || gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                .anyMatch(permanent -> permanent.getId().equals(chosenTarget)
                        && gameQueryService.isPlaneswalker(gameData, permanent));
        if (!legal) throw new IllegalStateException("The token must attack that opponent or a planeswalker they control");
        List<UUID> chosenTargets = new ArrayList<>(context.chosenAttackTargets());
        chosenTargets.add(chosenTarget);
        continueOpponentAttackTargetChoices(gameData,
                new PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking(
                        context.controllerId(), context.entry(), context.copyEffect(), context.tokens(),
                        context.tokenOpponents(), chosenTargets));
    }

    private void continueOpponentAttackTargetChoices(GameData gameData,
                                                      PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking context) {
        List<UUID> chosenTargets = new ArrayList<>(context.chosenAttackTargets());
        while (chosenTargets.size() < context.tokenOpponents().size()) {
            UUID opponentId = context.tokenOpponents().get(chosenTargets.size());
            List<UUID> planeswalkerIds = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                    .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                    .map(Permanent::getId).toList();
            if (planeswalkerIds.isEmpty()) {
                chosenTargets.add(opponentId);
                continue;
            }
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking(
                            context.controllerId(), context.entry(), context.copyEffect(), context.tokens(),
                            context.tokenOpponents(), chosenTargets));
            playerInputService.beginAnyTargetChoice(gameData, context.controllerId(), planeswalkerIds,
                    List.of(opponentId), "Choose the player or planeswalker for the token to attack.");
            return;
        }
        putPreparedTokenCopiesOntoBattlefield(gameData, context.entry(), context.tokens(), null,
                context.controllerId(), context.copyEffect(), chosenTargets, true);
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
        token.getPersistentGrantedKeywords().add(Keyword.HASTE);
        return token;
    }

    private List<UUID> putPreparedTokenCopiesOntoBattlefield(GameData gameData, StackEntry entry,
                                                            List<Permanent> tokens, Permanent sourcePermanent,
                                                            UUID tokenControllerId,
                                                            CreateTokenCopyOfTargetPermanentEffect effect,
                                                            List<UUID> expandedAttackTargets,
                                                            boolean explicitAttackTargets) {
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
                                tokens, sourcePermanent, expandedAttackTargets, explicitAttackTargets, i),
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
            battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, tokenControllerId, tokenPermanent, enterTappedTypes, simultaneouslyEntered);
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
            if (effect.tappedAndAttacking()) {
                tokenPermanent.enterAttacking(true);
                if (explicitAttackTargets && tokenIndex < expandedAttackTargets.size()) {
                    tokenPermanent.setAttackTarget(expandedAttackTargets.get(tokenIndex));
                } else if (!explicitAttackTargets && sourcePermanent != null) {
                    tokenPermanent.setAttackTarget(sourcePermanent.getAttackTarget());
                }
            }
            tokenIndex++;

            if (effect.exileAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
            }
            if (effect.exileAtEndOfCombat()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));
            }
            if (effect.sacrificeAtEndStep()) {
                // Only the player who created the token sacrifices it, and only while they control it
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP,
                        false, null, null, tokenControllerId));
            }
            if (effect.sacrificeAtNextUpkeep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_NEXT_UPKEEP));
            }

            Card sourceCard = tokenPermanent.getCard();
            gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
        }
        for (Permanent tokenPermanent : tokens) {
            Card sourceCard = tokenPermanent.getCard();
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, tokenControllerId, sourceCard, null, false);
            if (sourceCard.isSaga()) {
                sagaChapterService.initializeSaga(gameData, tokenPermanent, sourceCard, tokenControllerId);
            }
            if (effect.initialCounters() != null && !effect.initialCounters().isEmpty()
                    && !gameQueryService.cantHaveCounters(gameData, tokenPermanent)) {
                for (var counterEntry : effect.initialCounters().entrySet()) {
                    if (counterEntry.getValue() > 0) {
                        permanentCounterSupport.placeCounterOnPermanent(
                                gameData, entry, tokenPermanent, counterEntry.getKey(), counterEntry.getValue());
                    }
                }
            }
        }

        battlefieldEntryService.checkAllyTokenEntersTriggers(
                gameData, tokenControllerId, tokens.stream().map(Permanent::getId).toList());
        return createdIds;
    }

    /** Resumes the token-copy batch after one legal, untargeted Aura attachment choice. */
    public void completeTokenCopyAttachmentChoice(GameData gameData, UUID attachmentId,
            PermanentChoiceContext.PreparedTokenCopyAttachments context) {
        context.tokens().get(context.choosingIndex()).setAttachedTo(attachmentId);
        putPreparedTokenCopiesOntoBattlefield(gameData, context.entry(), context.tokens(),
                context.sourcePermanent(), context.controllerId(), context.copyEffect(),
                context.attackTargets(), context.explicitAttackTargets());
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
        boolean hasPTOverride = effect.powerOverride() != null || effect.toughnessOverride() != null;

        Card tokenCard = new Card();
        tokenCard.setName(effect.nameOverride() != null ? effect.nameOverride() : sourceCard.getName());
        tokenCard.setType(sourceCard.getType());
        tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
        tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
        tokenCard.setToken(true);
        if (sourceCard.getBackFaceCard() != null && !sourceCard.isModalDoubleFaced()
                && !sourceCard.getBackFaceCard().hasType(CardType.INSTANT)
                && !sourceCard.getBackFaceCard().hasType(CardType.SORCERY)) {
            tokenCard.setBackFaceCard(buildTokenCopyCard(sourceCard.getBackFaceCard(), effect,
                    isCreatureSubtype, targetingSourceCard));
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
                for (CardSubtype subtype : effect.additionalSubtypes()) {
                    if (!subtypes.contains(subtype)) {
                        subtypes.add(subtype);
                    }
                }
            }
        }
        tokenCard.setSubtypes(subtypes);

        if (effect.additionalTypes() != null && !effect.additionalTypes().isEmpty()) {
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
            effect.additionalSlotEffects().forEach((slot, effects) ->
                    effects.forEach(additionalEffect -> tokenCard.addEffect(slot, additionalEffect,
                            com.github.laxika.magicalvibes.model.TriggerMode.INDEPENDENT)));
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
