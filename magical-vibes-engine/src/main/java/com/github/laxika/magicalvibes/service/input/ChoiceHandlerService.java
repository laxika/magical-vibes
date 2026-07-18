package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorPutOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.GrantBasicLandTypeToTargetEffectHandler;
import java.util.Collections;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final WarpWorldService warpWorldService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final com.github.laxika.magicalvibes.service.state.StateBasedActionService stateBasedActionService;
    private final LegendRuleService legendRuleService;
    private final EffectResolutionService effectResolutionService;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;
    private final com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport lifeSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport damageSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport permanentControlSupport;

    public void handleListChoice(GameData gameData, Player player, String colorName) {
        if (gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class) == null) {
            throw new IllegalStateException("Not awaiting color choice");
        }
        PendingInteraction.ColorChoice colorChoice =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (colorChoice == null || !player.getId().equals(colorChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Mana color choice (Chromatic Star, etc.)
        if (colorChoice.context() instanceof ChoiceContext.ManaColorChoice ctx) {
            handleManaColorChosen(gameData, player, colorName, ctx);
            return;
        }

        // Attack mana split choice (Grand Warlord Radha, etc.)
        if (colorChoice.context() instanceof ChoiceContext.AttackManaSplitChoice ctx) {
            handleAttackManaSplitChosen(gameData, player, colorName, ctx);
            return;
        }

        // Card name choice (Pithing Needle, etc.)
        if (colorChoice.context() instanceof ChoiceContext.CardNameChoice ctx) {
            handleCardNameChosen(gameData, player, colorName, ctx);
            return;
        }

        // Text-changing effects (Mind Bend, etc.) — two-step color/land-type choice
        if (colorChoice.context() instanceof ChoiceContext.TextChangeFromWord ctx) {
            handleTextChangeFromWordChosen(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.TextChangeToWord ctx) {
            handleTextChangeToWordChosen(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.DrawReplacementChoice ctx) {
            handleDrawReplacementChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.KeywordGrantChoice ctx) {
            handleKeywordGrantChoice(gameData, player, colorName, ctx);
            return;
        }

        if (colorChoice.context() instanceof ChoiceContext.ExileByNameChoice ctx) {
            handleExileByNameChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ProtectionColorChoice ctx) {
            handleProtectionColorChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.MassProtectionColorChoice ctx) {
            handleMassProtectionColorChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ColorSetChoice ctx) {
            handleColorSetChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.DiscardChosenColorChoice ctx) {
            handleDiscardChosenColorChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ExileTopCardsChosenColorTokensChoice ctx) {
            handleExileTopCardsChosenColorTokensChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.CreateTokensPerPermanentOfChosenColorChoice ctx) {
            handleCreateTokensPerPermanentOfChosenColorChoice(gameData, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.SubtypeChoice ctx) {
            handleSubtypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.SpellCreatureTypeChoice ctx) {
            handleSpellCreatureTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ManaValueParityChoice ctx) {
            handleManaValueParityChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.NumberChoice ctx) {
            handleNumberChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.RemoveCountersForManaChoice ctx) {
            handleRemoveCountersForManaChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.TetravusCounterRemoval ctx) {
            handleTetravusCounterRemoval(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.PrimalClayFormChoice ctx) {
            handlePrimalClayFormChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.BasicLandTypeChoice ctx) {
            handleBasicLandTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.AddBasicLandTypeChoice ctx) {
            handleAddBasicLandTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.OwnLandsBecomeBasicTypeChoice ctx) {
            handleOwnLandsBecomeBasicTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.PermanentTypeChoice ctx) {
            handlePermanentTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.EachPlayerCardNameRevealChoice ctx) {
            handleEachPlayerCardNameRevealChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.SphinxAmbassadorNameChoice ctx) {
            handleSphinxAmbassadorNameChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.StorageMatrixUntapChoice ctx) {
            handleStorageMatrixUntapChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.BecomeChosenColorsChoice ctx) {
            handleBecomeChosenColorsChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.NameCardMillGainLifeChoice ctx) {
            handleNameCardMillGainLifeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.TargetPlayerNameCardRevealTopChoice ctx) {
            handleTargetPlayerNameCardRevealTopChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.RelicBindModeChoice ctx) {
            handleRelicBindModeChoice(gameData, player, colorName, ctx);
            return;
        }
        CardColor color = CardColor.valueOf(colorName);
        UUID permanentId = colorChoice.permanentId();
        UUID etbTargetId = colorChoice.etbTargetId();

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
        if (perm != null) {
            perm.setChosenColor(color);

            String logEntry = player.getUsername() + " chooses " + color.name().toLowerCase() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), color, perm.getCard().getName());

            if (gameQueryService.isCreature(gameData, perm)) {
                battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), etbTargetId, false);
            }
        }

        // CR 603.8 — the chosen color can immediately satisfy a state-triggered ability
        // condition (e.g. Lurebound Scarecrow controlling no permanents of the chosen color).
        stateBasedActionService.performStateBasedActions(gameData);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleManaColorChosen(GameData gameData, Player player, String colorName, ChoiceContext.ManaColorChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.interaction.clearAwaitingInput();

        ManaPool manaPool = gameData.playerManaPools.get(ctx.playerId());
        int amount = ctx.amount();
        if (ctx.spellOrAbilitySubtype()) {
            // "Any combination of colors" — add 1 mana of the chosen color per choice
            manaPool.addSubtypeSpellOrAbilityMana(ctx.restrictedToCreatureSubtype(), manaColor, 1);

            String subtypeLabel = ctx.restrictedToCreatureSubtype().getDisplayName();
            String logEntry = player.getUsername() + " adds one " + colorName.toLowerCase()
                    + " mana (" + subtypeLabel + " spells or abilities only).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds one {} {}-spell-or-ability mana", gameData.id, player.getUsername(), colorName.toLowerCase(), subtypeLabel);

            // If more mana to choose, prompt again for the next color
            int remaining = amount - 1;
            if (remaining > 0) {
                ChoiceContext.ManaColorChoice nextCtx = ChoiceContext.ManaColorChoice.subtypeSpellOrAbility(
                        ctx.playerId(), remaining, ctx.restrictedToCreatureSubtype());
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        ctx.playerId(), null, null, nextCtx, colors, "Choose a color of mana to add."));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        } else if (ctx.flashbackOnly()) {
            // "Any combination of colors" — add 1 mana of the chosen color per choice
            manaPool.addFlashbackOnlyMana(manaColor, 1);

            String logEntry = player.getUsername() + " adds one " + colorName.toLowerCase() + " mana (flashback only).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds one {} flashback-only mana", gameData.id, player.getUsername(), colorName.toLowerCase());

            // If more mana to choose, prompt again for the next color
            int remaining = amount - 1;
            if (remaining > 0) {
                ChoiceContext.ManaColorChoice nextCtx = new ChoiceContext.ManaColorChoice(ctx.playerId(), ctx.fromCreature(), remaining, null, true);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        ctx.playerId(), null, null, nextCtx, colors, "Choose a color of mana to add (flashback only)."));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        } else if (ctx.fixedColorOptions() != null) {
            // Filter lands ("Add {R}{R}, {R}{G}, or {G}{G}") — each mana is chosen individually from
            // the fixed color list; add one and re-prompt until all picks have been made.
            manaPool.add(manaColor, 1);
            if (ctx.fromCreature()) {
                manaPool.addCreatureMana(manaColor, 1);
            }

            String logEntry = player.getUsername() + " adds one " + colorName.toLowerCase() + " mana.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds one {} mana (fixed color combination)", gameData.id, player.getUsername(), colorName.toLowerCase());

            int remaining = amount - 1;
            if (remaining > 0) {
                ChoiceContext.ManaColorChoice nextCtx = ChoiceContext.ManaColorChoice.fixedColorCombination(
                        ctx.playerId(), ctx.fromCreature(), remaining, ctx.fixedColorOptions());
                List<String> colors = ctx.fixedColorOptions().stream().map(Enum::name).toList();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        ctx.playerId(), null, null, nextCtx, colors, "Choose a color of mana to add."));
                gameBroadcastService.broadcastGameState(gameData);
                return;
            }
        } else if (ctx.restrictedToCreatureSubtype() != null) {
            manaPool.addSubtypeCreatureMana(ctx.restrictedToCreatureSubtype(), manaColor, amount);
        } else if (ctx.instantSorceryOnly()) {
            manaPool.addInstantSorceryOnlyColored(manaColor, amount);
        } else {
            manaPool.add(manaColor, amount);
            if (ctx.fromCreature()) {
                manaPool.addCreatureMana(manaColor, amount);
            }
        }

        if (!ctx.flashbackOnly() && !ctx.spellOrAbilitySubtype() && ctx.fixedColorOptions() == null) {
            String manaWord = amount == 1 ? "one" : String.valueOf(amount);
            String logEntry = player.getUsername() + " adds " + manaWord + " " + colorName.toLowerCase() + " mana.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds {} {} mana", gameData.id, player.getUsername(), manaWord, colorName.toLowerCase());
        }

        gameData.priorityPassedBy.clear();
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }
        gameBroadcastService.broadcastGameState(gameData);
        // Resume any remaining effects of the spell/ability that paused for this mana-color choice
        // (e.g. Manamorphose: "Add two mana in any combination of colors. Draw a card.").
        resumeAndAutoPass(gameData);
    }

    private void handleAttackManaSplitChosen(GameData gameData, Player player, String colorName, ChoiceContext.AttackManaSplitChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.interaction.clearAwaitingInput();

        ManaPool manaPool = gameData.playerManaPools.get(ctx.playerId());
        // Add as persistent mana — doesn't drain at step/phase transitions until end of turn
        manaPool.addPersistentMana(manaColor, ctx.attackerCount());

        String logEntry = player.getUsername() + " adds " + ctx.attackerCount() + " " + colorName.toLowerCase() + " mana.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} adds {} {} mana (attacking creatures, persistent until end of turn)",
                gameData.id, player.getUsername(), ctx.attackerCount(), colorName.toLowerCase());

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleTextChangeFromWordChosen(GameData gameData, Player player, String chosenWord, ChoiceContext.TextChangeFromWord ctx) {
        // Only words that were actually offered are legal — Glamerdye offers color words only, so a
        // basic land type must be rejected even though it is a valid word for Mind Bend.
        PendingInteraction.ColorChoice active = gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (active != null && !active.options().contains(chosenWord)) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }
        boolean isColor = GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord);
        boolean isLandType = GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord);
        if (!isColor && !isLandType) {
            throw new IllegalArgumentException("Invalid choice: " + chosenWord);
        }

        ChoiceContext.TextChangeToWord choiceContext =
                new ChoiceContext.TextChangeToWord(ctx.targetId(), chosenWord, isColor);

        List<String> remainingOptions;
        String promptType;
        if (isColor) {
            remainingOptions = GameQueryService.TEXT_CHANGE_COLOR_WORDS.stream().filter(c -> !c.equals(chosenWord)).toList();
            promptType = "color word";
        } else {
            remainingOptions = GameQueryService.TEXT_CHANGE_LAND_TYPES.stream().filter(t -> !t.equals(chosenWord)).toList();
            promptType = "basic land type";
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                player.getId(), null, null, choiceContext, remainingOptions, "Choose the replacement " + promptType + "."));
        log.info("Game {} - Awaiting {} to choose replacement word for text change", gameData.id, player.getUsername());
    }

    private void handleTextChangeToWordChosen(GameData gameData, Player player, String chosenWord, ChoiceContext.TextChangeToWord ctx) {
        if (ctx.isColor()) {
            if (!GameQueryService.TEXT_CHANGE_COLOR_WORDS.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid color choice: " + chosenWord);
            }
        } else {
            if (!GameQueryService.TEXT_CHANGE_LAND_TYPES.contains(chosenWord)) {
                throw new IllegalArgumentException("Invalid land type choice: " + chosenWord);
            }
        }

        gameData.interaction.clearAwaitingInput();

        String fromText = textChangeChoiceToWord(ctx.fromWord());
        String toText = textChangeChoiceToWord(chosenWord);

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            target.getTextReplacements().add(new TextReplacement(fromText, toText));

            // If the permanent has a chosenColor matching the from-color, update it
            if (ctx.isColor()) {
                CardColor fromColor = CardColor.valueOf(ctx.fromWord());
                CardColor toColor = CardColor.valueOf(chosenWord);
                if (fromColor.equals(target.getChosenColor())) {
                    target.setChosenColor(toColor);
                }
            }

            String logEntry = player.getUsername() + " changes all instances of " + fromText + " to " + toText + " on " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} changes {} to {} on {}", gameData.id, player.getUsername(), fromText, toText, target.getCard().getName());
        } else {
            // Glamerdye may target a spell still on the stack; record the change so it carries onto the
            // permanent that spell resolves into (CR 613.7). For instants/sorceries it is a no-op.
            StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, ctx.targetId());
            if (targetSpell != null) {
                gameData.spellTextReplacements
                        .computeIfAbsent(ctx.targetId(), k -> new ArrayList<>())
                        .add(new TextReplacement(fromText, toText));
                String logEntry = player.getUsername() + " changes all instances of " + fromText + " to " + toText + " on " + targetSpell.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} changes {} to {} on spell {}", gameData.id, player.getUsername(), fromText, toText, targetSpell.getCard().getName());
            }
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private String textChangeChoiceToWord(String choice) {
        return switch (choice) {
            case "WHITE" -> "white";
            case "BLUE" -> "blue";
            case "BLACK" -> "black";
            case "RED" -> "red";
            case "GREEN" -> "green";
            case "PLAINS" -> "Plains";
            case "ISLAND" -> "Island";
            case "SWAMP" -> "Swamp";
            case "MOUNTAIN" -> "Mountain";
            case "FOREST" -> "Forest";
            default -> throw new IllegalArgumentException("Invalid choice: " + choice);
        };
    }

    private void handleCardNameChosen(GameData gameData, Player player, String cardName, ChoiceContext.CardNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        Card card = ctx.card();
        UUID controllerId = ctx.controllerId();

        Permanent perm = new Permanent(card);
        perm.setChosenName(cardName);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        Card enteredCard = perm.getCard();

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.playerChoosesForCard(player.getUsername(), cardName, enteredCard));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, enteredCard.getName(), playerName);
        log.info("Game {} - {} chooses card name \"{}\" for {}", gameData.id, player.getUsername(), cardName, card.getName());

        legendRuleService.checkLegendRule(gameData, controllerId);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleKeywordGrantChoice(GameData gameData, Player player, String chosenKeywordName, ChoiceContext.KeywordGrantChoice ctx) {
        Keyword keyword;
        try {
            keyword = Keyword.valueOf(chosenKeywordName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid keyword choice: " + chosenKeywordName);
        }
        if (!ctx.options().contains(keyword)) {
            throw new IllegalArgumentException("Keyword not among valid options: " + chosenKeywordName);
        }

        gameData.interaction.clearAwaitingInput();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            target.getGrantedKeywords().add(keyword);

            String keywordName = keyword.name().charAt(0) + keyword.name().substring(1).toLowerCase().replace('_', ' ');
            String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), keywordName, target.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    /**
     * Relic Bind's "choose one" modal triggered ability, resolved after the enchanted artifact
     * became tapped. The chosen mode's targeted effect is handed to the shared
     * {@link PermanentChoiceContext.MayAbilityTriggerTarget} flow: DAMAGE targets any player or
     * planeswalker, LIFE targets any player. A legal target always exists (both modes can hit a
     * player), so the ability never fizzles for lack of one.
     */
    private void handleRelicBindModeChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.RelicBindModeChoice ctx) {
        if (!ChoiceContext.RelicBindModeChoice.OPTIONS.contains(chosen)) {
            throw new IllegalArgumentException("Invalid Relic Bind mode: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        // The modal trigger's single effect has fully resolved into this choice; nothing remains to
        // resume on the RelicBindTapEffect entry, so drop the resume pointer before the target step.
        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;

        boolean damageMode = ChoiceContext.RelicBindModeChoice.DAMAGE.equals(chosen);
        CardEffect modeEffect = damageMode
                ? new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)
                : new TargetPlayerGainsLifeEffect(1);

        List<UUID> validTargets = new ArrayList<>(gameData.orderedPlayerIds);
        if (damageMode) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (permanent.getCard().hasType(CardType.PLANESWALKER)) {
                        validTargets.add(permanent.getId());
                    }
                }
            }
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                ctx.sourceCard(), ctx.controllerId(), List.of(modeEffect)));
        String targetDescription = damageMode ? "player or planeswalker" : "player";
        playerInputService.beginPermanentChoice(gameData, ctx.controllerId(), validTargets,
                ctx.sourceCard().getName() + " — Choose target " + targetDescription + ".");

        String logEntry = player.getUsername() + " chooses \"" + chosen + "\" for " + ctx.sourceCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        gameBroadcastService.broadcastGameState(gameData);
    }

    private void handleDrawReplacementChoice(GameData gameData, String chosenKind, ChoiceContext.DrawReplacementChoice ctx) {
        if (ctx.kind() != DrawReplacementKind.ABUNDANCE) {
            throw new IllegalStateException("Unsupported draw replacement choice kind: " + ctx.kind());
        }
        boolean chooseLand;
        if ("LAND".equals(chosenKind)) {
            chooseLand = true;
        } else if ("NONLAND".equals(chosenKind)) {
            chooseLand = false;
        } else {
            throw new IllegalArgumentException("Invalid Abundance choice: " + chosenKind);
        }

        UUID playerId = ctx.playerId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        gameData.interaction.clearAwaitingInput();

        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no cards to reveal for Abundance."));
            finalizeAfterDrawReplacementChoice(gameData);
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card chosenCard = null;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            revealed.add(top);
            boolean isLand = top.hasType(CardType.LAND);
            if ((chooseLand && isLand) || (!chooseLand && !isLand)) {
                chosenCard = top;
                break;
            }
        }

        String revealedNames = String.join(", ", revealed.stream().map(Card::getName).toList());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + revealedNames + " for Abundance."));

        List<Card> toBottom = new ArrayList<>(revealed);
        if (chosenCard != null) {
            gameData.addCardToHand(playerId, chosenCard);
            toBottom.remove(chosenCard);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + chosenCard.getName() + " into their hand."));
        } else {
            String missingKind = chooseLand ? "land" : "nonland";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals no " + missingKind + " card for Abundance."));
        }

        if (toBottom.size() == 1) {
            deck.add(toBottom.getFirst());
        } else if (toBottom.size() > 1) {
            gameData.pendingLibraryBottomReorders.addLast(new LibraryBottomReorderRequest(playerId, toBottom));
            if (!gameData.interaction.isAwaitingInput()) {
                warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            }
        }

        finalizeAfterDrawReplacementChoice(gameData);
    }

    private void finalizeAfterDrawReplacementChoice(GameData gameData) {
        gameData.priorityPassedBy.clear();
        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }
        gameBroadcastService.broadcastGameState(gameData);
        if (!gameData.interaction.isAwaitingInput()) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleProtectionColorChoice(GameData gameData, Player player, String chosenValue, ChoiceContext.ProtectionColorChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            if ("ARTIFACT".equals(chosenValue)) {
                target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
                String logEntry = target.getCard().getName() + " gains protection from artifacts until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} gains protection from artifacts until end of turn", gameData.id, target.getCard().getName());
            } else {
                CardColor color = CardColor.valueOf(chosenValue);
                target.getProtectionFromColorsUntilEndOfTurn().add(color);
                String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
                String logEntry = target.getCard().getName() + " gains protection from " + colorName.toLowerCase() + " until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), colorName.toLowerCase());
            }
        }

        // CR 704.5n/704.5q — the new protection can make an attached aura or equipment illegal
        stateBasedActionService.performStateBasedActions(gameData);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handleMassProtectionColorChoice(GameData gameData, Player player, String chosenValue,
            ChoiceContext.MassProtectionColorChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        CardColor color = CardColor.valueOf(chosenValue);
        String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();

        // The controller gains protection from the chosen color until end of turn.
        gameData.playerProtectionFromColorsUntilEndOfTurn
                .computeIfAbsent(ctx.controllerId(), k -> new java.util.HashSet<>())
                .add(color);

        // Each permanent the controller controls gains protection from the chosen color.
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                permanent.getProtectionFromColorsUntilEndOfTurn().add(color);
            }
        }

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        String logEntry = playerName + " and each permanent they control gain protection from "
                + colorName.toLowerCase() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} and their permanents gain protection from {} until end of turn",
                gameData.id, playerName, colorName.toLowerCase());

        // CR 704.5n/704.5q — the new protection can make attached auras or equipment illegal
        stateBasedActionService.performStateBasedActions(gameData);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handleColorSetChoice(GameData gameData, String chosenValue, ChoiceContext.ColorSetChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            CardColor color = CardColor.valueOf(chosenValue);

            // CR 613 layer engine: "becomes [color] until end of turn" is a floating layer-5
            // color-setting effect. We reuse GrantColorUntilEndOfTurnEffect (the L5 setter the
            // layered pass already understands) as the wrapped effect, and dual-write the legacy
            // transient-color fields for direct Permanent.getEffectiveColor callers, exactly as
            // GrantColorUntilEndOfTurnEffectHandler does.
            target.getTransientColors().clear();
            target.getTransientColors().add(color);
            target.setColorOverridden(true);
            gameData.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                    UUID.randomUUID(), ctx.sourceCardName(), null, ctx.controllerId(),
                    new com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect(color),
                    target.getId(), null, null,
                    com.github.laxika.magicalvibes.model.effect.EffectDuration.UNTIL_END_OF_TURN, 0));

            String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
            String logEntry = target.getCard().getName() + " becomes " + colorName + " until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handleDiscardChosenColorChoice(GameData gameData, String chosenValue,
            ChoiceContext.DiscardChosenColorChoice ctx) {
        CardColor color = CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        UUID controllerId = ctx.controllerId();
        UUID targetPlayerId = ctx.targetPlayerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String colorLabel = color.name().charAt(0) + color.name().substring(1).toLowerCase();

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " chooses " + colorLabel.toLowerCase()
                    + ". " + targetName + " reveals an empty hand."));
        } else {
            String cardNames = String.join(", ", hand.stream().map(Card::getName).toList());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " chooses " + colorLabel.toLowerCase()
                    + ". " + targetName + " reveals their hand: " + cardNames + "."));
        }

        // A card is "of that color" per its actual color (Scryfall colors array, honouring
        // hybrid/multicolor). Lands are excluded: the oracle loader derives a colorless land's
        // "colors" from its color identity (e.g. Forest -> green), but a Forest is a colorless card
        // and must not be discarded. Genuinely colored lands (color indicator) don't exist this era.
        List<Card> toDiscard = hand == null ? List.of()
                : new ArrayList<>(hand.stream()
                        .filter(c -> !c.hasType(CardType.LAND) && c.getColors().contains(color))
                        .toList());
        if (!toDiscard.isEmpty()) {
            gameData.discardCausedByOpponent = !targetPlayerId.equals(controllerId);
            hand.removeAll(toDiscard);
            for (Card card : toDiscard) {
                graveyardService.addCardToGraveyard(gameData, targetPlayerId, card);
                triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " discards " + toDiscard.size()
                    + " " + colorLabel.toLowerCase() + " card" + (toDiscard.size() != 1 ? "s" : "") + "."));
            log.info("Game {} - {} discards {} {} card(s) to Persecute-style effect",
                    gameData.id, targetName, toDiscard.size(), colorLabel.toLowerCase());
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " has no " + colorLabel.toLowerCase()
                    + " cards to discard."));
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    /**
     * Oona, Queen of the Fae: the controller chose a color; the target opponent exiles the top
     * {@code count} cards of their library and the controller creates one token per exiled card of
     * the chosen color. A card is "of the chosen color" per its printed colors, with lands excluded
     * (an oracle-loaded land derives its colors from color identity, so a colorless Island would
     * otherwise wrongly count as blue — mirrors Persecute's handling).
     */
    private void handleExileTopCardsChosenColorTokensChoice(GameData gameData, String chosenValue,
            ChoiceContext.ExileTopCardsChosenColorTokensChoice ctx) {
        CardColor color = CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        UUID controllerId = ctx.controllerId();
        UUID targetPlayerId = ctx.targetPlayerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String colorLabel = color.name().charAt(0) + color.name().substring(1).toLowerCase();

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        int toExile = library == null ? 0 : Math.min(ctx.count(), library.size());
        int matches = 0;
        for (int i = 0; i < toExile; i++) {
            Card card = library.removeFirst();
            gameData.addToExile(targetPlayerId, card);
            if (!card.hasType(CardType.LAND) && card.getColors().contains(color)) {
                matches++;
            }
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " chooses " + colorLabel.toLowerCase()
                + ". " + targetName + " exiles the top " + toExile + " card" + (toExile != 1 ? "s" : "")
                + " of their library."));
        log.info("Game {} - Oona: {} exiles {} card(s); {} of chosen colour {}",
                gameData.id, targetName, toExile, matches, colorLabel.toLowerCase());

        if (matches > 0) {
            permanentControlSupport.applyCreateToken(gameData, controllerId, ctx.tokenTemplate(), matches, ctx.sourceSetCode());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " creates " + matches
                    + " Faerie Rogue token" + (matches != 1 ? "s" : "") + "."));
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    /**
     * Rith, the Awakener: the controller chose a color; count every permanent of that color on the
     * battlefield (any controller) and create one token per match. A permanent is "of the chosen
     * color" per its effective colors, with lands excluded (an oracle-loaded land derives its colors
     * from color identity, so a colorless Forest would otherwise wrongly count as green — mirrors
     * Oona's handling).
     */
    private void handleCreateTokensPerPermanentOfChosenColorChoice(GameData gameData, String chosenValue,
            ChoiceContext.CreateTokensPerPermanentOfChosenColorChoice ctx) {
        CardColor color = CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        UUID controllerId = ctx.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String colorLabel = color.name().charAt(0) + color.name().substring(1).toLowerCase();

        int[] matches = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().hasType(CardType.LAND)) {
                return;
            }
            if (gameQueryService.getEffectiveColors(gameData, permanent).contains(color)) {
                matches[0]++;
            }
        });

        int count = matches[0];
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " chooses "
                + colorLabel.toLowerCase() + "."));
        log.info("Game {} - Rith: chosen colour {}, {} matching permanent(s)",
                gameData.id, colorLabel.toLowerCase(), count);

        if (count > 0) {
            permanentControlSupport.applyCreateToken(gameData, controllerId, ctx.tokenTemplate(), count, ctx.sourceSetCode());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " creates " + count
                    + " Saproling token" + (count != 1 ? "s" : "") + "."));
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    /**
     * Prismwake Merrow: accumulate the controller's color picks. Each color adds to the running set
     * and re-prompts (with a "DONE" option); "DONE", a repeated color, or all five colors finalizes
     * the choice — the target then becomes those colors until end of turn.
     */
    private void handleBecomeChosenColorsChoice(GameData gameData, Player player, String chosenValue,
            ChoiceContext.BecomeChosenColorsChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        List<CardColor> chosen = new ArrayList<>(ctx.chosen());
        if (!"DONE".equals(chosenValue)) {
            CardColor color = CardColor.valueOf(chosenValue);
            // A repeated color (e.g. a naive AI re-picking the same color) ends the choice rather
            // than looping forever; otherwise add it and, if fewer than five are chosen, re-prompt.
            if (!chosen.contains(color)) {
                chosen.add(color);
                if (chosen.size() < CardColor.values().length) {
                    playerInputService.beginBecomeChosenColorsChoice(gameData, player.getId(),
                            ctx.targetId(), ctx.sourceCardName(), chosen);
                    gameBroadcastService.broadcastGameState(gameData);
                    return;
                }
            }
        }

        applyBecomeChosenColors(gameData, ctx, player.getId(), chosen);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void applyBecomeChosenColors(GameData gameData, ChoiceContext.BecomeChosenColorsChoice ctx,
            UUID controllerId, List<CardColor> colors) {
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target == null || colors.isEmpty()) {
            return;
        }

        Set<CardColor> colorSet = new LinkedHashSet<>(colors);

        // CR 613 layer engine: "becomes [colors] until end of turn" is a floating layer-5
        // color-setting effect with its own timestamp. The legacy transient fields are still written
        // for direct Permanent.getEffectiveColor callers; the layered pass replays the setter.
        target.getTransientColors().clear();
        target.getTransientColors().addAll(colorSet);
        target.setColorOverridden(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                ctx.sourceCardName(), null, controllerId,
                new BecomeChosenColorsUntilEndOfTurnEffect(colorSet),
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String colorList = colorSet.stream()
                .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                .reduce((a, b) -> a + " and " + b).orElse("");
        String logEntry = target.getCard().getName() + " becomes " + colorList + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorList);
    }

    /**
     * Resumes resolving any remaining effects on the spell/ability that paused for this choice,
     * then continues the normal auto-pass flow.
     */
    private void resumeAndAutoPass(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleSubtypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.SubtypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenSubtype(subtype);

            String logEntry = player.getUsername() + " chooses " + subtype.getDisplayName() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses creature type {} for {}", gameData.id, player.getUsername(), subtype, perm.getCard().getName());

            // The subtype choice deferred the permanent's ETB triggers (they were skipped while input
            // was pending). Now that the type is chosen, process them — e.g. Brass Herald's "reveal the
            // top four cards" trigger, which reads the chosen type from the permanent.
            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), null, true);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleSpellCreatureTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.SpellCreatureTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.chosenSpellSubtype = subtype;
        gameData.interaction.clearAwaitingInput();

        String logEntry = player.getUsername() + " chooses " + subtype.getDisplayName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chooses creature type {} for a spell", gameData.id, player.getUsername(), subtype);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handleManaValueParityChoice(GameData gameData, Player player, String parityName, ChoiceContext.ManaValueParityChoice ctx) {
        com.github.laxika.magicalvibes.model.ManaValueParity parity =
                com.github.laxika.magicalvibes.model.ManaValueParity.valueOf(parityName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenManaValueParity(parity);

            String logEntry = player.getUsername() + " chooses " + parityName.toLowerCase() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), parityName.toLowerCase(), perm.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleNumberChoice(GameData gameData, Player player, String numberName, ChoiceContext.NumberChoice ctx) {
        int chosen = Integer.parseInt(numberName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenNumber(chosen);

            String logEntry = player.getUsername() + " chooses " + chosen + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses number {} for {}", gameData.id, player.getUsername(), chosen, perm.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        // Resumes the paused upkeep may-ability resolution when present; otherwise auto-passes
        // (the "as this enters" ETB choice, which has no pending stack-effect resolution).
        resumeAndAutoPass(gameData);
    }

    private void handleRemoveCountersForManaChoice(GameData gameData, Player player, String numberName,
                                                   ChoiceContext.RemoveCountersForManaChoice ctx) {
        int chosen = Integer.parseInt(numberName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null && chosen > 0) {
            // Storage land: remove the chosen counters (the ability's cost) and add that much mana
            // of the given color (times the Mana Reflection multiplier).
            int available = perm.getCounterCount(ctx.counterType());
            int removed = Math.min(chosen, available);
            perm.setCounterCount(ctx.counterType(), available - removed);

            int mana = removed * ctx.manaMultiplier();
            ManaPool pool = gameData.playerManaPools.get(ctx.playerId());
            pool.add(ctx.color(), mana);
            if (ctx.fromCreature()) {
                pool.addCreatureMana(ctx.color(), mana);
            }

            String logEntry = player.getUsername() + " removes " + removed + " "
                    + ctx.counterType().name().toLowerCase() + " counter(s) from " + perm.getCard().getName()
                    + " and adds " + mana + " " + ctx.color().getCode() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} removes {} {} counters and adds {} {} mana", gameData.id,
                    player.getUsername(), removed, ctx.counterType(), mana, ctx.color());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handleTetravusCounterRemoval(GameData gameData, Player player, String numberName,
                                              ChoiceContext.TetravusCounterRemoval ctx) {
        int chosen = Integer.parseInt(numberName);

        gameData.interaction.clearAwaitingInput();

        Permanent source = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (source != null && chosen > 0) {
            // Remove the chosen number of +1/+1 counters and create that many Tetravite tokens,
            // recording each as "created with" this Tetravus so the paired exile trigger sees them.
            int available = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            int removed = Math.min(chosen, available);
            source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, available - removed);

            List<UUID> createdIds = permanentControlSupport.applyCreateToken(gameData, player.getId(),
                    ctx.tokenTemplate(), removed, source.getCard().getSetCode());
            gameData.tetravusCreatedTokens
                    .computeIfAbsent(ctx.permanentId(), k -> ConcurrentHashMap.<UUID>newKeySet())
                    .addAll(createdIds);

            String logEntry = player.getUsername() + " removes " + removed + " +1/+1 counter"
                    + (removed == 1 ? "" : "s") + " from " + source.getCard().getName()
                    + " to create " + removed + " Tetravite token" + (removed == 1 ? "" : "s") + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} removes {} +1/+1 counters from {} to create {} Tetravite tokens",
                    gameData.id, player.getUsername(), removed, source.getCard().getName(), removed);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handlePrimalClayFormChoice(GameData gameData, Player player, String formName, ChoiceContext.PrimalClayFormChoice ctx) {
        com.github.laxika.magicalvibes.model.PrimalClayForm form =
                com.github.laxika.magicalvibes.model.PrimalClayForm.valueOf(formName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            // "As this creature enters, it becomes ..." — lock in the chosen shape's base P/T,
            // keyword, and extra creature type for the life of the permanent (CR 614.1c).
            perm.setBasePowerOverriddenPermanently(true);
            perm.setPermanentBasePowerOverride(form.power());
            perm.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
            perm.setBaseToughnessOverriddenPermanently(true);
            perm.setPermanentBaseToughnessOverride(form.toughness());
            perm.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());
            if (form.keyword() != null) {
                perm.getPersistentGrantedKeywords().add(form.keyword());
            }
            if (form.subtype() != null) {
                perm.getGrantedSubtypes().add(form.subtype());
            }

            String logEntry = player.getUsername() + " chooses a " + form.power() + "/" + form.toughness()
                    + (form.keyword() != null ? " " + form.keyword().name().toLowerCase() : "")
                    + " shape for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses shape {} for {}", gameData.id, player.getUsername(), form, perm.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleBasicLandTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.BasicLandTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenSubtype(subtype);

            String logEntry = player.getUsername() + " chooses " + subtype.getDisplayName() + " for " + perm.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses basic land type {} for {}", gameData.id, player.getUsername(), subtype, perm.getCard().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleAddBasicLandTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.AddBasicLandTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent targetLand = gameQueryService.findPermanentById(gameData, ctx.targetLandId());
        if (targetLand != null) {
            GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(targetLand, subtype, ctx.duration(), ctx.replacing());

            String logEntry = GrantBasicLandTypeToTargetEffectHandler.describeBasicLandTypeChange(
                    targetLand, subtype, ctx.duration(), ctx.replacing());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} becomes a {} (replacing={})", gameData.id, targetLand.getCard().getName(), subtype, ctx.replacing());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleOwnLandsBecomeBasicTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.OwnLandsBecomeBasicTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().hasType(CardType.LAND)) {
                    GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(
                            permanent, subtype, EffectDuration.UNTIL_END_OF_TURN, true);
                }
            }
        }

        String playerName = gameData.playerIdToName.get(ctx.controllerId());
        String logEntry = "Each land " + playerName + " controls becomes a "
                + subtype.getDisplayName() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Each land {} controls becomes a {} until end of turn", gameData.id, playerName, subtype);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handlePermanentTypeChoice(GameData gameData, Player player, String typeName, ChoiceContext.PermanentTypeChoice ctx) {
        CardType chosenType = CardType.valueOf(typeName);
        if (!chosenType.isPermanentType()) {
            throw new IllegalArgumentException("Invalid permanent type choice: " + typeName);
        }

        gameData.interaction.clearAwaitingInput();

        UUID controllerId = ctx.controllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        List<Card> toReturn = new ArrayList<>();
        for (Card card : graveyard) {
            if (card.hasType(chosenType)) {
                toReturn.add(card);
            }
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        if (!toReturn.isEmpty()) {
            List<String> returnedNames = new ArrayList<>();
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : toReturn) {
                    graveyard.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                    gameData.addCardToHand(controllerId, card);
                    returnedNames.add(card.getName());
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }

            String logEntry = playerName + " chooses " + chosenType.getDisplayName()
                    + " and returns " + String.join(", ", returnedNames) + " to hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} chooses {} and returns {} card(s) from graveyard to hand",
                    gameData.id, playerName, chosenType.getDisplayName(), returnedNames.size());
        } else {
            String logEntry = playerName + " chooses " + chosenType.getDisplayName()
                    + " but has no " + chosenType.getDisplayName().toLowerCase() + " cards in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleStorageMatrixUntapChoice(GameData gameData, Player player, String typeName,
                                                ChoiceContext.StorageMatrixUntapChoice ctx) {
        com.github.laxika.magicalvibes.model.filter.PermanentPredicate restrict = switch (typeName) {
            case "ARTIFACT" -> new com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate();
            case "CREATURE" -> new com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate();
            case "LAND" -> new com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate();
            default -> throw new IllegalArgumentException("Invalid Storage Matrix untap choice: " + typeName);
        };

        gameData.interaction.clearAwaitingInput();

        String playerName = gameData.playerIdToName.get(ctx.playerId());
        String logEntry = playerName + " chooses " + typeName.toLowerCase() + " (Storage Matrix): only "
                + typeName.toLowerCase() + " permanents untap this step.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chooses {} for Storage Matrix untap", gameData.id, playerName, typeName);

        turnProgressionService.resumeStorageMatrixUntap(gameData, ctx.playerId(), restrict);
    }

    private void handleEachPlayerCardNameRevealChoice(GameData gameData, Player player, String cardName,
                                                      ChoiceContext.EachPlayerCardNameRevealChoice ctx) {
        // Store this player's chosen name
        Map<UUID, String> updatedNames = new LinkedHashMap<>(ctx.chosenNames());
        updatedNames.put(player.getId(), cardName);

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" (each player name/reveal)",
                gameData.id, player.getUsername(), cardName);

        // Check if more players need to name a card
        UUID nextPlayerId = null;
        for (UUID pid : ctx.playerOrder()) {
            if (!updatedNames.containsKey(pid)) {
                nextPlayerId = pid;
                break;
            }
        }

        if (nextPlayerId != null) {
            // Prompt next player
            gameData.interaction.clearAwaitingInput();

            var nextContext = new ChoiceContext.EachPlayerCardNameRevealChoice(
                    ctx.playerOrder(), updatedNames);

            List<String> cardNames = collectAllCardNamesInGame(gameData);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    nextPlayerId, null, null, nextContext, cardNames, "Choose a card name."));

            String nextPlayerName = gameData.playerIdToName.get(nextPlayerId);
            log.info("Game {} - Awaiting {} to choose a card name (each player name/reveal)",
                    gameData.id, nextPlayerName);
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }

        // All players have named — resolve reveals
        gameData.interaction.clearAwaitingInput();

        for (UUID pid : ctx.playerOrder()) {
            String chosenName = updatedNames.get(pid);
            String playerName = gameData.playerIdToName.get(pid);
            List<Card> deck = gameData.playerDecks.get(pid);

            if (deck == null || deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                continue;
            }

            Card topCard = deck.removeFirst();
            String revealLog = playerName + " reveals " + topCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(revealLog));

            if (topCard.getName().equals(chosenName)) {
                gameData.addCardToHand(pid, topCard);
                String handLog = playerName + " puts " + topCard.getName() + " into their hand.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(handLog));
                log.info("Game {} - {} guessed correctly, {} goes to hand", gameData.id, playerName, topCard.getName());
            } else {
                deck.add(topCard);
                String bottomLog = playerName + " puts " + topCard.getName() + " on the bottom of their library.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(bottomLog));
                log.info("Game {} - {} guessed wrong, {} goes to bottom", gameData.id, playerName, topCard.getName());
            }
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleNameCardMillGainLifeChoice(GameData gameData, Player player, String cardName,
                                                  ChoiceContext.NameCardMillGainLifeChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" (name/mill/gain life)",
                gameData.id, player.getUsername(), cardName);

        // Peek the card that is about to be milled so we can inspect it after the mill resolves.
        List<Card> deck = gameData.playerDecks.get(ctx.targetPlayerId());
        Card topCard = (deck != null && !deck.isEmpty()) ? deck.getFirst() : null;

        graveyardService.resolveMillPlayer(gameData, ctx.targetPlayerId(), 1);

        // "If a card with the chosen name was milled this way" — the card must have both matched the
        // chosen name and actually reached the graveyard (a replacement effect could redirect it).
        if (topCard != null && topCard.getName().equals(cardName)) {
            List<Card> graveyard = gameData.playerGraveyards.get(ctx.targetPlayerId());
            boolean reachedGraveyard = graveyard != null
                    && graveyard.stream().anyMatch(c -> c.getId().equals(topCard.getId()));
            if (reachedGraveyard) {
                int manaValue = topCard.getManaValue();
                lifeSupport.applyGainLife(gameData, ctx.controllerId(), manaValue);
                String controllerName = gameData.playerIdToName.get(ctx.controllerId());
                String lifeLog = controllerName + " gains " + manaValue + " life.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(lifeLog));
                log.info("Game {} - {} milled the named card {}, {} gains {} life",
                        gameData.id, gameData.playerIdToName.get(ctx.targetPlayerId()),
                        topCard.getName(), controllerName, manaValue);
            }
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void handleTargetPlayerNameCardRevealTopChoice(GameData gameData, Player player, String cardName,
                                                           ChoiceContext.TargetPlayerNameCardRevealTopChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        UUID targetPlayerId = ctx.targetPlayerId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" (name-card-reveal-top)",
                gameData.id, player.getUsername(), cardName);

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + "'s library is empty."));
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            resumeAndAutoPass(gameData);
            return;
        }

        Card topCard = deck.getFirst();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " reveals " + topCard.getName() + "."));

        if (topCard.getName().equals(cardName)) {
            deck.removeFirst();
            gameData.addCardToHand(targetPlayerId, topCard);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " puts " + topCard.getName() + " into their hand."));
            log.info("Game {} - {} named correctly, {} goes to hand", gameData.id, targetName, topCard.getName());
        } else {
            graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " puts " + topCard.getName() + " into their graveyard."));
            dealRevealMissDamage(gameData, ctx, targetPlayerId);
            log.info("Game {} - {} named incorrectly, {} goes to graveyard", gameData.id, targetName, topCard.getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        resumeAndAutoPass(gameData);
    }

    private void dealRevealMissDamage(GameData gameData, ChoiceContext.TargetPlayerNameCardRevealTopChoice ctx,
                                      UUID targetPlayerId) {
        if (ctx.damageOnMiss() <= 0) return;

        Permanent source = gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId());
        if (source == null) return;

        StackEntry damageEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                source.getCard(),
                ctx.controllerId(),
                source.getCard().getName() + "'s ability",
                List.of(),
                targetPlayerId,
                ctx.sourcePermanentId());

        damageSupport.dealDamageToPlayer(gameData, damageEntry, targetPlayerId, ctx.damageOnMiss());
    }

    private List<String> collectAllCardNamesInGame(GameData gameData) {
        Set<String> names = new TreeSet<>();
        for (UUID pid : gameData.playerIds) {
            gameData.playerBattlefields.getOrDefault(pid, List.of())
                    .forEach(p -> names.add(p.getCard().getName()));
            gameData.playerHands.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerGraveyards.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.playerDecks.getOrDefault(pid, List.of())
                    .forEach(c -> names.add(c.getName()));
            gameData.getPlayerExiledCards(pid)
                    .forEach(c -> names.add(c.getName()));
        }
        gameData.stack.forEach(se -> names.add(se.getCard().getName()));
        return new ArrayList<>(names);
    }

    private void handleExileByNameChoice(GameData gameData, Player player, String cardName, ChoiceContext.ExileByNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        UUID targetPlayerId = ctx.targetPlayerId();
        UUID controllerId = ctx.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = controllerName + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" for exile from zones", gameData.id, controllerName, cardName);

        // Collect all matching cards across hand, graveyard, and library
        List<Card> matchingCards = new ArrayList<>();

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand != null) {
            matchingCards.addAll(hand.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            matchingCards.addAll(graveyard.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            matchingCards.addAll(library.stream().filter(c -> c.getName().equals(cardName)).toList());
        }

        if (matchingCards.isEmpty()) {
            // No matching cards — just shuffle library and resolve
            if (library != null) {
                Collections.shuffle(library);
            }

            String exileLog = controllerName + " exiles 0 cards named \"" + cardName + "\" from " + targetName
                    + "'s hand, graveyard, and library. " + targetName + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} found 0 cards named \"{}\" in {}'s zones", gameData.id, controllerName, cardName, targetName);

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        // Present matching cards for "any number" selection
        playerInputService.beginMultiZoneExileChoice(gameData, controllerId, matchingCards, targetPlayerId, cardName);
        gameBroadcastService.broadcastGameState(gameData);
    }

    private void handleSphinxAmbassadorNameChoice(GameData gameData, Player player, String cardName, ChoiceContext.SphinxAmbassadorNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        PendingSphinxAmbassadorChoice pending = gameData.peekPendingInteraction(PendingSphinxAmbassadorChoice.class);
        if (pending == null || pending.selectedCard() == null) {
            throw new IllegalStateException("No pending Sphinx Ambassador choice");
        }

        Card selectedCard = pending.selectedCard();
        UUID controllerId = pending.controllerId();
        UUID targetPlayerId = pending.targetPlayerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = targetName + " chooses \"" + cardName + "\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" for Sphinx Ambassador", gameData.id, targetName, cardName);

        boolean isCreature = selectedCard.hasType(CardType.CREATURE);
        boolean nameDoesNotMatch = !selectedCard.getName().equals(cardName);

        if (isCreature && nameDoesNotMatch) {
            // Conditions met — present may ability to controller: "You may put it onto the battlefield"
            String prompt = pending.sourceCard().getName() + " — Put " + selectedCard.getName()
                    + " onto the battlefield under your control?";
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    pending.sourceCard(), controllerId,
                    List.of(new SphinxAmbassadorPutOnBattlefieldEffect()),
                    prompt
            ));
            playerInputService.processNextMayAbility(gameData);
        } else {
            // Conditions not met — card stays in library without being revealed (per ruling)
            String logMsg = "The conditions for " + pending.sourceCard().getName() + " are not met. "
                    + targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            log.info("Game {} - Sphinx Ambassador: selected card does not match conditions (creature={}, nameMatch={})",
                    gameData.id, isCreature, !nameDoesNotMatch);

            gameData.playerDecks.get(targetPlayerId).add(selectedCard);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameData.clearPendingInteractions(PendingSphinxAmbassadorChoice.class);

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void handleMultiZoneExileCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class) == null) {
            throw new IllegalStateException("Not awaiting multi-zone exile choice");
        }
        PendingInteraction.MultiZoneExileChoice ctx =
                gameData.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class);
        if (ctx == null || !player.getId().equals(ctx.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        // Validate selected card IDs against valid set
        List<UUID> validIds = ctx.validCardIds();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        gameData.interaction.clearAwaitingInput();

        UUID targetPlayerId = ctx.targetPlayerId();
        UUID controllerId = ctx.controllerId();
        String cardName = ctx.cardName();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Set<UUID> selectedIds = new java.util.HashSet<>(cardIds);
        int exiledCount = 0;

        // Remove selected cards from hand
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand != null) {
            List<Card> toExile = hand.stream().filter(c -> selectedIds.contains(c.getId())).toList();
            hand.removeAll(toExile);
            for (Card card : toExile) {
                gameData.addToExile(targetPlayerId, card);
            }
            exiledCount += toExile.size();
        }

        // Remove selected cards from graveyard
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            List<Card> toExile = graveyard.stream().filter(c -> selectedIds.contains(c.getId())).toList();
            graveyard.removeAll(toExile);
            for (Card card : toExile) {
                gameData.addToExile(targetPlayerId, card);
            }
            if (!toExile.isEmpty()) {
                graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
            }
            exiledCount += toExile.size();
        }

        // Remove selected cards from library
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            List<Card> toExile = library.stream().filter(c -> selectedIds.contains(c.getId())).toList();
            library.removeAll(toExile);
            for (Card card : toExile) {
                gameData.addToExile(targetPlayerId, card);
            }
            exiledCount += toExile.size();
        }

        // Always shuffle target player's library
        if (library != null) {
            Collections.shuffle(library);
        }

        String exileLog = controllerName + " exiles " + exiledCount + " card" + (exiledCount != 1 ? "s" : "")
                + " named \"" + cardName + "\" from " + targetName + "'s hand, graveyard, and library. "
                + targetName + " shuffles their library.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
        log.info("Game {} - {} exiled {} card(s) named \"{}\" from {}'s zones",
                gameData.id, controllerName, exiledCount, cardName, targetName);

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }
}


