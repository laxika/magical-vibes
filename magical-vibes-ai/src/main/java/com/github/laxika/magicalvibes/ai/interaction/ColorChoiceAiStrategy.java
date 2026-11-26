package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.RegisterChosenNameCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Answers the COLOR_CHOICE family (mana color, card name, keyword, subtype, permanent type,
 * basic land type, Abundance land/nonland, …). Ported verbatim from the legacy
 * {@code AiChoiceHandler.handleColorChoice} heuristic — the specific variant is read off the
 * record's {@link PendingInteraction.ColorChoice#context()}.
 */
@Slf4j
class ColorChoiceAiStrategy implements AiInteractionStrategy<PendingInteraction.ColorChoice> {

    @Override
    public Class<PendingInteraction.ColorChoice> handledType() {
        return PendingInteraction.ColorChoice.class;
    }

    @Override
    public void answer(PendingInteraction.ColorChoice interaction, AiInteractionContext ctx) {
        UUID aiPlayerId = ctx.aiPlayerId();
        if (!aiPlayerId.equals(interaction.playerId())) {
            return;
        }

        GameData gameData = ctx.gameData();
        UUID gameId = ctx.gameId();
        ChoiceContext context = interaction.context();

        if (context instanceof ChoiceContext.DrawReplacementChoice drc
                && drc.kind() == DrawReplacementKind.ABUNDANCE) {
            log.info("AI: Choosing NONLAND for Abundance in game {}", gameId);
            answerWith(ctx, interaction, "NONLAND");
            return;
        }

        if (context instanceof ChoiceContext.CoinFlipResultChoice
                || context instanceof ChoiceContext.EntryCoinFlipResultChoice) {
            // Heads wins a called flip (and gives an entering Molten Sentry its attacking form), so the AI keeps it.
            log.info("AI: Keeping heads for a Krark's Thumb coin flip in game {}", gameId);
            answerWith(ctx, interaction, ChoiceContext.CoinFlipResultChoice.HEADS);
            return;
        }

        if (context instanceof ChoiceContext.KeywordGrantChoice kgc) {
            String chosenKeyword = kgc.options().getFirst().name();
            log.info("AI: Choosing keyword {} in game {}", chosenKeyword, gameId);
            answerWith(ctx, interaction, chosenKeyword);
            return;
        }

        if (context instanceof ChoiceContext.ReplacementEffectOrder order) {
            String chosen = ReplacementOrderChoice.choose(order, interaction.options());
            log.info("AI: Applying replacement effect \"{}\" next in game {}", chosen, gameId);
            answerWith(ctx, interaction, chosen);
            return;
        }

        if (context instanceof ChoiceContext.DeathExileReplacementChoice) {
            // Options keeping the card come first, then the plain exile that gives no rider to anyone.
            String chosen = interaction.options().getFirst();
            log.info("AI: Choosing death replacement \"{}\" in game {}", chosen, gameId);
            answerWith(ctx, interaction, chosen);
            return;
        }

        if (context instanceof ChoiceContext.SplitFreeCastMoreTargetsChoice) {
            // Optional extra targets of a free split half: cast with the targets already chosen.
            String done = interaction.options().getLast();
            log.info("AI: Choosing \"{}\" for optional split-half targets in game {}", done, gameId);
            answerWith(ctx, interaction, done);
            return;
        }

        if (context instanceof ChoiceContext.ChooseModeChoice
                || context instanceof ChoiceContext.StackedModalTriggerModeChoice
                || context instanceof ChoiceContext.LibraryCastModeChoice
                || context instanceof ChoiceContext.SplitFreeCastHalfChoice) {
            // Modal abilities and free-cast modal spells take the first option.
            String chosenMode = interaction.options().getFirst();
            log.info("AI: Choosing modal option \"{}\" in game {}", chosenMode, gameId);
            answerWith(ctx, interaction, chosenMode);
            return;
        }

        if (context instanceof ChoiceContext.ExileFreeCastModeChoice modeChoice) {
            String chosenMode = modeChoice.chosenModeIndices().size() >= modeChoice.effect().choicesRequired()
                    && interaction.options().contains(
                    com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.FINISH_MODE_SELECTION)
                    ? com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.FINISH_MODE_SELECTION
                    : interaction.options().getFirst();
            log.info("AI: Choosing exile free-cast modal option \"{}\" in game {}", chosenMode, gameId);
            answerWith(ctx, interaction, chosenMode);
            return;
        }

        if (context instanceof ChoiceContext.CardNameChoice) {
            UUID opponentId = getOpponentId(gameData, aiPlayerId);
            List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                    .filter(permanent -> !permanent.isFaceDown()).toList();
            String chosenName = opponentField.stream()
                    .filter(p -> !p.getCard().getActivatedAbilities(Zone.BATTLEFIELD).isEmpty())
                    .map(p -> p.getCard().getName())
                    .filter(interaction.options()::contains)
                    .findFirst()
                    .orElseGet(() -> knownName(ctx, interaction, opponentId));
            log.info("AI: Choosing card name \"{}\" in game {}", chosenName, gameId);
            answerWith(ctx, interaction, chosenName);
            return;
        }

        if (context instanceof ChoiceContext.DualCardNameChoice) {
            // Null Chamber: lock down something seen of an opponent's.
            String chosenName = knownName(ctx, interaction, getOpponentId(gameData, aiPlayerId));
            log.info("AI: Choosing card name \"{}\" to lock down in game {}", chosenName, gameId);
            answerWith(ctx, interaction, chosenName);
            return;
        }

        if (context instanceof ChoiceContext.EachPlayerCardNameRevealChoice
                || context instanceof ChoiceContext.TargetPlayerNameCardRevealTopChoice) {
            // For the reveal-top-card game: the name our library holds most of, its order being unknown to us.
            String chosenName = mostHeldName(ctx, interaction.options(),
                    gameData.playerDecks.getOrDefault(aiPlayerId, List.of()))
                    .orElseGet(() -> choosableName(ctx, interaction).orElse("Island"));
            log.info("AI: Choosing card name \"{}\" for reveal in game {}", chosenName, gameId);
            answerWith(ctx, interaction, chosenName);
            return;
        }

        if (context instanceof ChoiceContext.ChooseCardNameAtResolutionChoice) {
            StackEntry resolving = gameData.pendingEffectResolutionEntry;
            String chosenName = searchesForChosenName(resolving)
                    ? knownName(ctx, interaction, resolving.getTargetId())
                    : nameAtResolution(ctx, interaction, resolving);
            log.info("AI: Choosing card name \"{}\" as an effect resolves in game {}", chosenName, gameId);
            answerWith(ctx, interaction, chosenName);
            return;
        }

        if (context instanceof ChoiceContext.SubtypeChoice) {
            List<String> options = interaction.options();
            if (options.isEmpty()) {
                return;
            }
            String chosenSubtype = options.contains("HUMAN") ? "HUMAN" : options.getFirst();
            log.info("AI: Choosing creature type {} in game {}", chosenSubtype, gameId);
            answerWith(ctx, interaction, chosenSubtype);
            return;
        }

        if (context instanceof ChoiceContext.SpellCreatureTypeChoice) {
            List<String> options = interaction.options();
            if (options.isEmpty()) {
                return;
            }
            String chosenSubtype = options.contains("HUMAN") ? "HUMAN" : options.getFirst();
            log.info("AI: Choosing creature type {} for a spell in game {}", chosenSubtype, gameId);
            answerWith(ctx, interaction, chosenSubtype);
            return;
        }

        if (context instanceof ChoiceContext.NumberChoice) {
            // Options are the numbers in range as strings; pick the middle option — a balanced pick
            // for Shapeshifter-style "power = N, toughness = max − N" cards (avoids a 0-toughness body).
            List<String> options = interaction.options();
            String chosenNumber = options.isEmpty() ? "0" : options.get(options.size() / 2);
            log.info("AI: Choosing number {} in game {}", chosenNumber, gameId);
            answerWith(ctx, interaction, chosenNumber);
            return;
        }

        if (context instanceof ChoiceContext.MoveCountersAmountChoice move) {
            // "Move / remove any number of counters": options are the lowest allowed answer up to N. Move or remove
            // them all, except a move from the AI's own permanent onto one it doesn't control (Forgotten Ancient's
            // "onto other creatures"), and a removal from a permanent that strips a counter helping its controller's
            // side or keeps one hurting it: the AI's own +1/+1 counters and an opponent's -1/-1 counters stay (Rhys,
            // the Evermore; Eventide's Shadow). A removal slot has no destination (or is a player's counters); what it
            // removes is counted by the steps after it (Galloping Lizrog, Sensational Spider-Man, Hex Parasite).
            ChoiceContext.CounterMoveSlot slot = move.slots().get(move.index());
            boolean givesAway = slot.toPermanentId() != null
                    && aiPlayerId.equals(ctx.gameQueryService()
                    .findPermanentController(gameData, slot.fromPermanentId()))
                    && !aiPlayerId.equals(ctx.gameQueryService()
                    .findPermanentController(gameData, slot.toPermanentId()));
            boolean keepsCounters = slot.toPermanentId() == null && slot.fromPermanentId() != null
                    && slot.counterType() != null && slot.counterType() != CounterType.ANY
                    && aiPlayerId.equals(ctx.gameQueryService()
                    .findPermanentController(gameData, slot.fromPermanentId()))
                    == helpsItsHolder(slot.counterType());
            List<String> options = interaction.options();
            String chosenNumber = options.isEmpty() ? "0"
                    : givesAway || keepsCounters ? options.getFirst() : options.get(options.size() - 1);
            log.info("AI: Choosing {} counters to {} in game {}", chosenNumber,
                    slot.toPermanentId() == null ? "remove" : "move", gameId);
            answerWith(ctx, interaction, chosenNumber);
            return;
        }

        if (context instanceof ChoiceContext.RemoveCountersForManaChoice) {
            // Storage land mana ability: options are 0..N storage counters. Remove them all for the
            // most mana (the AI only activates the ability when it wants the mana).
            List<String> options = interaction.options();
            String chosenNumber = options.isEmpty() ? "0" : options.get(options.size() - 1);
            log.info("AI: Removing {} counters for mana in game {}", chosenNumber, gameId);
            answerWith(ctx, interaction, chosenNumber);
            return;
        }

        if (context instanceof ChoiceContext.PrimalClayFormChoice) {
            String chosenForm = "THREE_THREE";
            log.info("AI: Choosing Primal Clay shape {} in game {}", chosenForm, gameId);
            answerWith(ctx, interaction, chosenForm);
            return;
        }

        if (context instanceof ChoiceContext.BasicLandTypeChoice) {
            String chosenType = "ISLAND";
            log.info("AI: Choosing basic land type {} in game {}", chosenType, gameId);
            answerWith(ctx, interaction, chosenType);
            return;
        }

        if (context instanceof ChoiceContext.AddBasicLandTypeChoice) {
            String chosenType = "ISLAND";
            log.info("AI: Choosing basic land type to add {} in game {}", chosenType, gameId);
            answerWith(ctx, interaction, chosenType);
            return;
        }

        if (context instanceof ChoiceContext.SnowLandwalkGrantChoice) {
            // Snow landwalk only turns on if an opponent controls a snow land of the chosen type,
            // so pick a type they actually have; otherwise the grant is worthless either way.
            String chosenType = findOpponentSnowLandType(gameData, aiPlayerId, interaction.options());
            log.info("AI: Choosing land type {} for snow landwalk in game {}", chosenType, gameId);
            answerWith(ctx, interaction, chosenType);
            return;
        }

        if (context instanceof ChoiceContext.LandsOfTypeBecomeBasicTypeChoice landTypeCtx) {
            // First pick: a land type currently on the board (prefer opponent's); second: Island.
            String chosenType = landTypeCtx.fromType() == null
                    ? findCommonLandTypeOnBoard(gameData, interaction.options())
                    : "ISLAND";
            log.info("AI: Choosing {} for lands-of-type become in game {}", chosenType, gameId);
            answerWith(ctx, interaction, chosenType);
            return;
        }

        if (context instanceof ChoiceContext.SphinxAmbassadorNameChoice) {
            // AI names the best creature card from its own library to try to guess what was picked
            List<Card> ownDeck = gameData.playerDecks.getOrDefault(aiPlayerId, List.of());
            String chosenName = ownDeck.stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .max(java.util.Comparator.comparingInt(Card::getManaValue))
                    .map(Card::getName)
                    .filter(interaction.options()::contains)
                    .orElseGet(() -> knownName(ctx, interaction, aiPlayerId));
            log.info("AI: Choosing card name \"{}\" for Sphinx Ambassador in game {}", chosenName, gameId);
            answerWith(ctx, interaction, chosenName);
            return;
        }

        if (context instanceof ChoiceContext.PermanentTypeChoice) {
            // Pick the permanent type with the most cards in our graveyard
            List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayerId, List.of());
            Map<CardType, Long> typeCounts = new HashMap<>();
            for (CardType type : List.of(CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.LAND, CardType.PLANESWALKER)) {
                typeCounts.put(type, graveyard.stream().filter(c -> c.hasType(type)).count());
            }
            CardType bestType = typeCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(CardType.CREATURE);
            log.info("AI: Choosing permanent type {} in game {}", bestType.name(), gameId);
            answerWith(ctx, interaction, bestType.name());
            return;
        }

        if (context instanceof ChoiceContext.StorageMatrixUntapChoice) {
            // Untap the type we have the most tapped permanents of; tie-break toward lands (mana).
            List<Permanent> field = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
            long tappedLands = field.stream().filter(p -> p.isTapped() && p.getCard().hasType(CardType.LAND)).count();
            long tappedCreatures = field.stream().filter(p -> p.isTapped() && p.getCard().hasType(CardType.CREATURE)).count();
            long tappedArtifacts = field.stream().filter(p -> p.isTapped() && p.getCard().hasType(CardType.ARTIFACT)).count();
            String chosenType = "LAND";
            long best = tappedLands;
            if (tappedCreatures > best) {
                best = tappedCreatures;
                chosenType = "CREATURE";
            }
            if (tappedArtifacts > best) {
                chosenType = "ARTIFACT";
            }
            log.info("AI: Choosing {} for Storage Matrix untap in game {}", chosenType, gameId);
            answerWith(ctx, interaction, chosenType);
            return;
        }

        if (context instanceof ChoiceContext.TeferisRealmTypeChoice) {
            // Phase out the type the opponent has the most of (excluding our own board preference
            // toward keeping our lands); prefer creature when tied.
            Map<String, Long> opponentCounts = new HashMap<>();
            for (var e : gameData.playerBattlefields.entrySet()) {
                if (e.getKey().equals(aiPlayerId)) {
                    continue;
                }
                for (Permanent p : e.getValue()) {
                    if (p.getCard().isToken()) {
                        continue;
                    }
                    if (p.getCard().hasType(CardType.CREATURE)) {
                        opponentCounts.merge("CREATURE", 1L, Long::sum);
                    }
                    if (p.getCard().hasType(CardType.ARTIFACT)) {
                        opponentCounts.merge("ARTIFACT", 1L, Long::sum);
                    }
                    if (p.getCard().hasType(CardType.LAND)) {
                        opponentCounts.merge("LAND", 1L, Long::sum);
                    }
                    if (p.getCard().hasType(CardType.ENCHANTMENT) && !p.getCard().isAura()) {
                        opponentCounts.merge("NON_AURA_ENCHANTMENT", 1L, Long::sum);
                    }
                }
            }
            String chosenType = opponentCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("CREATURE");
            log.info("AI: Choosing {} for Teferi's Realm in game {}", chosenType, gameId);
            answerWith(ctx, interaction, chosenType);
            return;
        }

        if (context instanceof ChoiceContext.AdjustCounterKindChoice acc) {
            // Quarry Hauler: for each kind of counter on the target, add or remove one. Grow our own
            // good counters / shrink opponents'; and the reverse for detrimental counters.
            CounterType kind = acc.remainingKinds().getFirst();
            boolean badCounter = kind == CounterType.MINUS_ONE_MINUS_ONE
                    || kind == CounterType.MINUS_ZERO_MINUS_ONE
                    || kind == CounterType.MINUS_ZERO_MINUS_TWO
                    || kind == CounterType.MINUS_TWO_MINUS_ONE
                    || kind == CounterType.STUN
                    || kind == CounterType.PARALYZATION;
            boolean ownTarget = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of()).stream()
                    .anyMatch(p -> p.getId().equals(acc.targetId()));
            String chosen = (ownTarget != badCounter)
                    ? ChoiceContext.AdjustCounterKindChoice.ADD
                    : ChoiceContext.AdjustCounterKindChoice.REMOVE;
            log.info("AI: {} a {} counter in game {}", chosen, kind, gameId);
            answerWith(ctx, interaction, chosen);
            return;
        }

        if (context instanceof ChoiceContext.TormentPenaltyChoice) {
            // Torment of Hailfire: lose life while comfortably ahead, otherwise preserve life by
            // pitching a card (then a permanent). The "lose life" option is the one that is neither
            // the sacrifice nor the discard label.
            List<String> options = interaction.options();
            String loseLife = options.stream()
                    .filter(o -> !o.equals(ChoiceContext.TormentPenaltyChoice.SACRIFICE)
                            && !o.equals(ChoiceContext.TormentPenaltyChoice.DISCARD))
                    .findFirst()
                    .orElse(options.getLast());
            String chosen;
            if (gameData.getLife(aiPlayerId) > 6) {
                chosen = loseLife;
            } else if (options.contains(ChoiceContext.TormentPenaltyChoice.DISCARD)) {
                chosen = ChoiceContext.TormentPenaltyChoice.DISCARD;
            } else if (options.contains(ChoiceContext.TormentPenaltyChoice.SACRIFICE)) {
                chosen = ChoiceContext.TormentPenaltyChoice.SACRIFICE;
            } else {
                chosen = loseLife;
            }
            log.info("AI: Choosing \"{}\" for Torment of Hailfire in game {}", chosen, gameId);
            answerWith(ctx, interaction, chosen);
            return;
        }

        if (context instanceof ChoiceContext.ForbiddenRitualPenaltyChoice) {
            // Same victim heuristics as Torment of Hailfire (any permanent, not only nonland).
            List<String> options = interaction.options();
            String loseLife = options.stream()
                    .filter(o -> !o.equals(ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE)
                            && !o.equals(ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD))
                    .findFirst()
                    .orElse(options.getLast());
            String chosen;
            if (gameData.getLife(aiPlayerId) > 6) {
                chosen = loseLife;
            } else if (options.contains(ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD)) {
                chosen = ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD;
            } else if (options.contains(ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE)) {
                chosen = ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE;
            } else {
                chosen = loseLife;
            }
            log.info("AI: Choosing \"{}\" for Forbidden Ritual in game {}", chosen, gameId);
            answerWith(ctx, interaction, chosen);
            return;
        }

        if (context instanceof ChoiceContext.WintersChillPaymentChoice) {
            // Save own attackers: prefer pay {2}, then {1}, then nothing.
            List<String> options = interaction.options();
            String chosen;
            if (options.contains(ChoiceContext.WintersChillPaymentChoice.PAY_TWO)) {
                chosen = ChoiceContext.WintersChillPaymentChoice.PAY_TWO;
            } else if (options.contains(ChoiceContext.WintersChillPaymentChoice.PAY_ONE)) {
                chosen = ChoiceContext.WintersChillPaymentChoice.PAY_ONE;
            } else {
                chosen = ChoiceContext.WintersChillPaymentChoice.PAY_NOTHING;
            }
            log.info("AI: Choosing \"{}\" for Winter's Chill in game {}", chosen, gameId);
            answerWith(ctx, interaction, chosen);
            return;
        }

        if (context instanceof ChoiceContext.AllLandsProduceChosenColorChoice) {
            // Hall of Gemstone locks our own lands too, so pick the color our hand needs most
            // rather than the usual "hose the opponent" heuristic.
            Map<CardColor, Long> handColors = gameData.playerHands.getOrDefault(aiPlayerId, List.of()).stream()
                    .map(Card::getColor)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
            String chosenColor = handColors.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().name())
                    .orElse(CardColor.GREEN.name());
            log.info("AI: Choosing color {} for the all-lands mana lock in game {}", chosenColor, gameId);
            answerWith(ctx, interaction, chosenColor);
            return;
        }

        // Pick the color that appears most on opponent's battlefield
        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        int[] colorCounts = new int[CardColor.values().length];
        for (Permanent perm : opponentField) {
            CardColor color = perm.getCard().getColor();
            if (color != null) {
                colorCounts[color.ordinal()]++;
            }
        }

        CardColor bestColor = CardColor.WHITE;
        int bestCount = 0;
        for (CardColor color : CardColor.values()) {
            if (colorCounts[color.ordinal()] > bestCount) {
                bestCount = colorCounts[color.ordinal()];
                bestColor = color;
            }
        }

        String bestColorName = bestColor.name();
        String chosen = interaction.options().isEmpty() || interaction.options().contains(bestColorName)
                ? bestColorName
                : interaction.options().getFirst();

        log.info("AI: Choosing list option {} in game {}", chosen, gameId);
        answerWith(ctx, interaction, chosen);
    }

    /**
     * Picks the offered land type that an opponent controls a snow land of, so the granted snow
     * landwalk actually makes the creature unblockable. Falls back to the first option.
     */
    private static String findOpponentSnowLandType(GameData gameData, UUID aiPlayerId, List<String> options) {
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            if (entry.getKey().equals(aiPlayerId)) {
                continue;
            }
            for (Permanent permanent : entry.getValue()) {
                Card card = permanent.getCard();
                if (!card.getSupertypes().contains(CardSupertype.SNOW)) {
                    continue;
                }
                for (String option : options) {
                    if (card.getSubtypes().contains(CardSubtype.valueOf(option))) {
                        return option;
                    }
                }
            }
        }
        return options.isEmpty() ? "ISLAND" : options.getFirst();
    }

    /**
     * Picks the offered land type that appears most often among lands on the battlefield,
     * so Vision Charm's conversion hits something. Falls back to the first option.
     */
    private static String findCommonLandTypeOnBoard(GameData gameData, List<String> options) {
        Map<String, Long> counts = new HashMap<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                Card card = permanent.getCard();
                if (!card.hasType(CardType.LAND)) {
                    continue;
                }
                for (String option : options) {
                    if (card.getSubtypes().contains(CardSubtype.valueOf(option))) {
                        counts.merge(option, 1L, Long::sum);
                    }
                }
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(options.isEmpty() ? "ISLAND" : options.getFirst());
    }

    /**
     * Sends {@code choice} as the answer, except that a card name a permanent's static ability says
     * can't be chosen (Glimpse, the Unthinkable) is swapped for a choosable one, since the engine
     * rejects it. The substitute comes from the offered options when there are any, otherwise from
     * the cards the AI can see (its library and hand, then the battlefield).
     */
    private static void answerWith(AiInteractionContext ctx, PendingInteraction.ColorChoice interaction,
                                   String choice) {
        String answer = choice;
        if (ChoiceHandlerService.isCardNameChoice(interaction.context())
                && ctx.gameQueryService().isCardNameUnchoosable(ctx.gameData(), choice)) {
            answer = choosableName(ctx, interaction).orElse(choice);
            log.info("AI: The name \"{}\" can't be chosen; choosing \"{}\" instead in game {}",
                    choice, answer, ctx.gameId());
        }
        ctx.gameActions().answerInteraction(new InteractionAnswer.ListChoiceMade(answer));
    }

    private static Optional<String> choosableName(AiInteractionContext ctx,
                                                  PendingInteraction.ColorChoice interaction) {
        GameData gameData = ctx.gameData();
        Stream<String> candidates;
        if (!interaction.options().isEmpty()) {
            candidates = interaction.options().stream();
        } else {
            Stream<Card> visibleCards = Stream.concat(
                    Stream.concat(
                            gameData.playerDecks.getOrDefault(ctx.aiPlayerId(), List.of()).stream(),
                            gameData.playerHands.getOrDefault(ctx.aiPlayerId(), List.of()).stream()),
                    gameData.orderedPlayerIds.stream()
                            .flatMap(id -> gameData.playerBattlefields.getOrDefault(id, List.of()).stream())
                            .map(Permanent::getCard));
            candidates = Stream.concat(visibleCards.map(Card::getName), Stream.of("Island"));
        }
        return candidates
                .filter(name -> !ctx.gameQueryService().isCardNameUnchoosable(gameData, name))
                .findFirst();
    }

    private static UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }

    /** Whether the resolution rewards its controller when a creature with the name being chosen connects. */
    private static boolean rewardsOwnNamedCreature(StackEntry resolving) {
        return resolving != null && resolving.getEffectsToResolve().stream()
                .anyMatch(RegisterChosenNameCombatDamageTriggerEffect.class::isInstance);
    }

    /** Whether the resolution searches the player it targets for cards with the name being chosen. */
    private static boolean searchesForChosenName(StackEntry resolving) {
        return resolving != null && resolving.getEffectsToResolve().stream()
                .anyMatch(effect -> effect instanceof ExileCardsWithSameNameEffect exile
                        && exile.permanents() == null && exile.cards() == null
                        && exile.searched() instanceof PlayerSubject.TargetPlayer);
    }

    /**
     * The offered, choosable name the AI's knowledge favors: what it holds most of in its own hand or library where the
     * effect reveals from there (a library's order being unknown to it), otherwise what is seen most of the other
     * players', then of any, then of its own cards.
     * No other player's hand or library and no hidden face is read (CR 400.2, 406.3).
     */
    private static String nameAtResolution(AiInteractionContext ctx, PendingInteraction.ColorChoice interaction,
                                           StackEntry resolving) {
        GameData gameData = ctx.gameData();
        UUID aiPlayerId = ctx.aiPlayerId();
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayerId, List.of());
        List<Card> library = gameData.playerDecks.getOrDefault(aiPlayerId, List.of());
        List<List<Card>> pools = new ArrayList<>();
        if (revealsOwnHand(resolving, aiPlayerId)) {
            pools.add(hand);
        } else if (revealsOwnLibrary(resolving, aiPlayerId)) {
            pools.add(library);
        }
        List<Card> seenOfOthers = new ArrayList<>();
        List<Card> seenOfOwn = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            (playerId.equals(aiPlayerId) ? seenOfOwn : seenOfOthers).addAll(publicCards(gameData, playerId));
        }
        if (rewardsOwnNamedCreature(resolving)) {
            pools.add(gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of()).stream()
                    .filter(permanent -> !permanent.isFaceDown()
                            && ctx.gameQueryService().isCreature(gameData, permanent))
                    .map(Permanent::getCard)
                    .toList());
        }
        pools.add(seenOfOthers);
        pools.add(seenOfOwn);
        pools.add(Stream.concat(hand.stream(), library.stream()).toList());
        return firstKnownName(ctx, interaction, pools);
    }

    /**
     * The offered, choosable name seen most of {@code aboutPlayerId}'s, otherwise of the AI's own cards. No other
     * player's hand or library and no hidden face is read (CR 400.2, 406.3).
     */
    private static String knownName(AiInteractionContext ctx, PendingInteraction.ColorChoice interaction,
                                    UUID aboutPlayerId) {
        GameData gameData = ctx.gameData();
        UUID aiPlayerId = ctx.aiPlayerId();
        List<List<Card>> pools = new ArrayList<>();
        if (aboutPlayerId != null) {
            pools.add(publicCards(gameData, aboutPlayerId));
        }
        pools.add(publicCards(gameData, aiPlayerId));
        pools.add(Stream.concat(gameData.playerHands.getOrDefault(aiPlayerId, List.of()).stream(),
                gameData.playerDecks.getOrDefault(aiPlayerId, List.of()).stream()).toList());
        return firstKnownName(ctx, interaction, pools);
    }

    private static String firstKnownName(AiInteractionContext ctx, PendingInteraction.ColorChoice interaction,
                                         List<List<Card>> pools) {
        for (List<Card> pool : pools) {
            Optional<String> name = mostHeldName(ctx, interaction.options(), pool);
            if (name.isPresent()) {
                return name.get();
            }
        }
        return choosableName(ctx, interaction).orElse("Island");
    }

    /** The offered, choosable name most of {@code cards} have; the earlier option among equals. */
    private static Optional<String> mostHeldName(AiInteractionContext ctx, List<String> options, List<Card> cards) {
        Map<String, Integer> held = new HashMap<>();
        cards.forEach(card -> card.getNames().forEach(name -> held.merge(name, 1, Integer::sum)));
        String best = null;
        for (String name : options) {
            if (held.containsKey(name) && (best == null || held.get(name) > held.get(best))
                    && !ctx.gameQueryService().isCardNameUnchoosable(ctx.gameData(), name)) {
                best = name;
            }
        }
        return Optional.ofNullable(best);
    }

    /** The cards of {@code playerId} face up on the battlefield, in the graveyard and in exile. */
    private static List<Card> publicCards(GameData gameData, UUID playerId) {
        List<Card> cards = new ArrayList<>();
        gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isFaceDown())
                .forEach(permanent -> cards.add(permanent.getCard()));
        cards.addAll(gameData.playerGraveyards.getOrDefault(playerId, List.of()));
        gameData.getPlayerExiledCards(playerId).stream()
                .filter(card -> !gameData.isExiledFaceDown(card.getId()))
                .forEach(cards::add);
        return cards;
    }

    private static boolean revealsOwnHand(StackEntry resolving, UUID aiPlayerId) {
        return resolving != null && resolving.getEffectsToResolve().stream()
                .anyMatch(effect -> effect instanceof RevealCardsFromHandEffect reveal
                        && namesAi(resolving, reveal.player(), aiPlayerId));
    }

    private static boolean revealsOwnLibrary(StackEntry resolving, UUID aiPlayerId) {
        return resolving != null && resolving.getEffectsToResolve().stream()
                .anyMatch(effect -> effect instanceof RevealTopCardOfLibraryEffect top
                        && namesAi(resolving, top.player(), aiPlayerId)
                        || effect instanceof MillEffect mill && namesAi(resolving, mill.player(), aiPlayerId)
                        || effect instanceof RevealCardsFromTopUntilEffect until
                        && namesAi(resolving, until.library(), aiPlayerId));
    }

    /** Whether {@code player} is the AI: "you" of the effect it controls, or the player targeted being the AI. */
    private static boolean namesAi(StackEntry resolving, PlayerSubject player, UUID aiPlayerId) {
        return player instanceof PlayerSubject.You && aiPlayerId.equals(resolving.getControllerId())
                || player instanceof PlayerSubject.TargetPlayer && aiPlayerId.equals(resolving.getTargetId());
    }

    /** Whether a counter of {@code kind} helps the permanent it is on; a -1/-1-style counter doesn't. */
    private static boolean helpsItsHolder(CounterType kind) {
        return kind != CounterType.MINUS_ONE_MINUS_ONE && kind != CounterType.MINUS_TWO_MINUS_ONE;
    }
}
