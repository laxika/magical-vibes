package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.ChosenFromListRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, "NONLAND"));
            return;
        }

        if (context instanceof ChoiceContext.KeywordGrantChoice kgc) {
            String chosenKeyword = kgc.options().getFirst().name();
            log.info("AI: Choosing keyword {} in game {}", chosenKeyword, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenKeyword));
            return;
        }

        if (context instanceof ChoiceContext.CardNameChoice) {
            UUID opponentId = getOpponentId(gameData, aiPlayerId);
            List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
            String chosenName = opponentField.stream()
                    .filter(p -> !p.getCard().getActivatedAbilities().isEmpty())
                    .map(p -> p.getCard().getName())
                    .findFirst()
                    .orElse(opponentField.isEmpty() ? "Pithing Needle" : opponentField.getFirst().getCard().getName());
            log.info("AI: Choosing card name \"{}\" in game {}", chosenName, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenName));
            return;
        }

        if (context instanceof ChoiceContext.EachPlayerCardNameRevealChoice
                || context instanceof ChoiceContext.TargetPlayerNameCardRevealTopChoice) {
            // For the reveal-top-card game: guess the top card of our library
            List<Card> aiDeck = gameData.playerDecks.getOrDefault(aiPlayerId, List.of());
            String chosenName = aiDeck.isEmpty() ? "Island" : aiDeck.getFirst().getName();
            log.info("AI: Choosing card name \"{}\" for reveal in game {}", chosenName, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenName));
            return;
        }

        if (context instanceof ChoiceContext.SubtypeChoice) {
            String chosenSubtype = "HUMAN";
            log.info("AI: Choosing creature type {} in game {}", chosenSubtype, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenSubtype));
            return;
        }

        if (context instanceof ChoiceContext.NumberChoice) {
            // Options are the numbers in range as strings; pick the middle option — a balanced pick
            // for Shapeshifter-style "power = N, toughness = max − N" cards (avoids a 0-toughness body).
            List<String> options = interaction.options();
            String chosenNumber = options.isEmpty() ? "0" : options.get(options.size() / 2);
            log.info("AI: Choosing number {} in game {}", chosenNumber, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenNumber));
            return;
        }

        if (context instanceof ChoiceContext.RemoveCountersForManaChoice) {
            // Storage land mana ability: options are 0..N storage counters. Remove them all for the
            // most mana (the AI only activates the ability when it wants the mana).
            List<String> options = interaction.options();
            String chosenNumber = options.isEmpty() ? "0" : options.get(options.size() - 1);
            log.info("AI: Removing {} counters for mana in game {}", chosenNumber, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenNumber));
            return;
        }

        if (context instanceof ChoiceContext.PrimalClayFormChoice) {
            String chosenForm = "THREE_THREE";
            log.info("AI: Choosing Primal Clay shape {} in game {}", chosenForm, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenForm));
            return;
        }

        if (context instanceof ChoiceContext.BasicLandTypeChoice) {
            String chosenType = "ISLAND";
            log.info("AI: Choosing basic land type {} in game {}", chosenType, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenType));
            return;
        }

        if (context instanceof ChoiceContext.AddBasicLandTypeChoice) {
            String chosenType = "ISLAND";
            log.info("AI: Choosing basic land type to add {} in game {}", chosenType, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenType));
            return;
        }

        if (context instanceof ChoiceContext.OwnLandsBecomeBasicTypeChoice) {
            String chosenType = "ISLAND";
            log.info("AI: Choosing basic land type for own lands {} in game {}", chosenType, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenType));
            return;
        }

        if (context instanceof ChoiceContext.SphinxAmbassadorNameChoice) {
            // AI names the best creature card from its own library to try to guess what was picked
            List<Card> ownDeck = gameData.playerDecks.getOrDefault(aiPlayerId, List.of());
            String chosenName = ownDeck.stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .max(java.util.Comparator.comparingInt(Card::getManaValue))
                    .map(Card::getName)
                    .orElse("Sphinx Ambassador");
            log.info("AI: Choosing card name \"{}\" for Sphinx Ambassador in game {}", chosenName, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenName));
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
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, bestType.name()));
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
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenType));
            return;
        }

        if (context instanceof ChoiceContext.ExileByNameChoice exile) {
            UUID targetId = exile.targetPlayerId();
            List<Card> targetHand = gameData.playerHands.getOrDefault(targetId, List.of());
            String chosenName = targetHand.stream()
                    .filter(c -> !exile.excludedTypes().contains(c.getType()))
                    .map(Card::getName)
                    .findFirst()
                    .orElse("Lightning Bolt");
            log.info("AI: Choosing card name \"{}\" for exile in game {}", chosenName, gameId);
            ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, chosenName));
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

        log.info("AI: Choosing color {} in game {}", bestColor.name(), gameId);
        ctx.gameActions().handleListChoice(ctx.selfConnection(), new ChosenFromListRequest(null, bestColor.name()));
    }

    private static UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }
}
