package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
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
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorPutOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.GrantBasicLandTypeToTargetEffectHandler;
import java.util.Collections;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final TurnProgressionService turnProgressionService;
    private final com.github.laxika.magicalvibes.service.state.StateBasedActionService stateBasedActionService;
    private final LegendRuleService legendRuleService;
    private final EffectResolutionService effectResolutionService;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;
    private final com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport lifeSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport playerInteractionSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport damageSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport permanentControlSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PhaseOutChosenTypeSupport phaseOutChosenTypeSupport;

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

        // Two-player card name choice (Null Chamber)
        if (colorChoice.context() instanceof ChoiceContext.DualCardNameChoice ctx) {
            handleDualCardNameChosen(gameData, player, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.RevealHandDamageAndExileByNameChoice ctx) {
            handleRevealHandDamageAndExileByNameChoice(gameData, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.AllLandsProduceChosenColorChoice ctx) {
            handleAllLandsProduceChosenColorChoice(gameData, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.PayAnyAmountOfLifeAsEnters ctx) {
            handlePayAnyAmountOfLifeAsEnters(gameData, player, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.SnowLandwalkGrantChoice ctx) {
            handleSnowLandwalkGrantChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.OwnLandsBecomeBasicTypeChoice ctx) {
            handleOwnLandsBecomeBasicTypeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.LandsOfTypeBecomeBasicTypeChoice ctx) {
            handleLandsOfTypeBecomeBasicTypeChoice(gameData, player, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.TeferisRealmTypeChoice ctx) {
            handleTeferisRealmTypeChoice(gameData, player, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.OpponentsCantCastNamedSpellsUntilNextTurnChoice ctx) {
            handleOpponentsCantCastNamedSpellsUntilNextTurnChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.NameCardMillDrawChoice ctx) {
            handleNameCardMillDrawChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ChooseNameExileTopRevealUntilNamedChoice ctx) {
            handleChooseNameExileTopRevealUntilNamedChoice(gameData, player, colorName, ctx);
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
        if (colorChoice.context() instanceof ChoiceContext.HullbreakerHorrorModeChoice ctx) {
            handleHullbreakerHorrorModeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.AdjustCounterKindChoice ctx) {
            handleAdjustCounterKindChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ChooseModeChoice ctx) {
            handleChooseModeChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.TormentPenaltyChoice ctx) {
            handleTormentPenaltyChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ForbiddenRitualPenaltyChoice ctx) {
            handleForbiddenRitualPenaltyChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.OathOfLimDulPenaltyChoice ctx) {
            handleOathOfLimDulPenaltyChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.WintersChillPaymentChoice ctx) {
            handleWintersChillPaymentChoice(gameData, player, colorName, ctx);
            return;
        }
        if (colorChoice.context() instanceof ChoiceContext.ForgottenLorePaymentChoice ctx) {
            handleForgottenLorePaymentChoice(gameData, player, colorName, ctx);
            return;
        }
        CardColor color = CardColor.valueOf(colorName);
        UUID permanentId = colorChoice.permanentId();
        UUID etbTargetId = colorChoice.etbTargetId();

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, permanentId);
        if (perm != null) {
            perm.setChosenColor(color);

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses " + color.name().toLowerCase() + " for " , perm.getCard(), "."));
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), color, perm.getCard().getName());

            if (gameQueryService.isCreature(gameData, perm)) {
                battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), etbTargetId, false);
            }
        }

        // CR 603.8 — the chosen color can immediately satisfy a state-triggered ability
        // condition (e.g. Lurebound Scarecrow controlling no permanents of the chosen color).
        stateBasedActionService.performStateBasedActions(gameData);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds one {} {}-spell-or-ability mana", gameData.id, player.getUsername(), colorName.toLowerCase(), subtypeLabel);

            // If more mana to choose, prompt again for the next color
            int remaining = amount - 1;
            if (remaining > 0) {
                ChoiceContext.ManaColorChoice nextCtx = ChoiceContext.ManaColorChoice.subtypeSpellOrAbility(
                        ctx.playerId(), remaining, ctx.restrictedToCreatureSubtype());
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        ctx.playerId(), null, null, nextCtx, colors, "Choose a color of mana to add."));
                inputCompletionService.publishStateAfterInput(gameData);
                return;
            }
        } else if (ctx.flashbackOnly()) {
            // "Any combination of colors" — add 1 mana of the chosen color per choice
            manaPool.addFlashbackOnlyMana(manaColor, 1);

            String logEntry = player.getUsername() + " adds one " + colorName.toLowerCase() + " mana (flashback only).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds one {} flashback-only mana", gameData.id, player.getUsername(), colorName.toLowerCase());

            // If more mana to choose, prompt again for the next color
            int remaining = amount - 1;
            if (remaining > 0) {
                ChoiceContext.ManaColorChoice nextCtx = new ChoiceContext.ManaColorChoice(ctx.playerId(), ctx.fromCreature(), remaining, null, true);
                List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        ctx.playerId(), null, null, nextCtx, colors, "Choose a color of mana to add (flashback only)."));
                inputCompletionService.publishStateAfterInput(gameData);
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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds one {} mana (fixed color combination)", gameData.id, player.getUsername(), colorName.toLowerCase());

            int remaining = amount - 1;
            if (remaining > 0) {
                ChoiceContext.ManaColorChoice nextCtx = ChoiceContext.ManaColorChoice.fixedColorCombination(
                        ctx.playerId(), ctx.fromCreature(), remaining, ctx.fixedColorOptions());
                List<String> colors = ctx.fixedColorOptions().stream().map(Enum::name).toList();
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        ctx.playerId(), null, null, nextCtx, colors, "Choose a color of mana to add."));
                inputCompletionService.publishStateAfterInput(gameData);
                return;
            }
        } else if (ctx.restrictedToCreatureSubtype() != null) {
            manaPool.addSubtypeCreatureMana(ctx.restrictedToCreatureSubtype(), manaColor, amount, ctx.grantsUncounterable());
        } else if (ctx.creatureSpellOnly()) {
            manaPool.addCreatureSpellOnlyMana(manaColor, amount);

            String logEntry = player.getUsername() + " adds " + (amount == 1 ? "one" : String.valueOf(amount))
                    + " " + colorName.toLowerCase() + " mana (creature spells only).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds {} {} creature-spell-only mana", gameData.id, player.getUsername(), amount, colorName.toLowerCase());
        } else if (ctx.instantSorceryOnly()) {
            manaPool.addInstantSorceryOnlyColored(manaColor, amount);
        } else {
            manaPool.add(manaColor, amount);
            if (ctx.fromCreature()) {
                manaPool.addCreatureMana(manaColor, amount);
            }
        }

        if (!ctx.flashbackOnly() && !ctx.spellOrAbilitySubtype() && ctx.fixedColorOptions() == null && !ctx.creatureSpellOnly()) {
            String manaWord = amount == 1 ? "one" : String.valueOf(amount);
            String logEntry = player.getUsername() + " adds " + manaWord + " " + colorName.toLowerCase() + " mana.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} adds {} {} mana", gameData.id, player.getUsername(), manaWord, colorName.toLowerCase());
        }

        // Resume any remaining effects of the spell/ability that paused for this mana-color choice
        // (e.g. Manamorphose: "Add two mana in any combination of colors. Draw a card.").
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleAttackManaSplitChosen(GameData gameData, Player player, String colorName, ChoiceContext.AttackManaSplitChoice ctx) {
        ManaColor manaColor = ManaColor.valueOf(colorName);

        gameData.interaction.clearAwaitingInput();

        ManaPool manaPool = gameData.playerManaPools.get(ctx.playerId());
        // Add as persistent mana — doesn't drain at step/phase transitions until end of turn
        manaPool.addPersistentMana(manaColor, ctx.attackerCount());

        String logEntry = player.getUsername() + " adds " + ctx.attackerCount() + " " + colorName.toLowerCase() + " mana.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} adds {} {} mana (attacking creatures, persistent until end of turn)",
                gameData.id, player.getUsername(), ctx.attackerCount(), colorName.toLowerCase());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
                new ChoiceContext.TextChangeToWord(ctx.targetId(), chosenWord, isColor, ctx.untilEndOfTurn());

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
            target.getTextReplacements().add(new TextReplacement(fromText, toText, ctx.untilEndOfTurn()));

            // If the permanent has a chosenColor matching the from-color, update it
            if (ctx.isColor()) {
                CardColor fromColor = CardColor.valueOf(ctx.fromWord());
                CardColor toColor = CardColor.valueOf(chosenWord);
                if (fromColor.equals(target.getChosenColor())) {
                    target.setChosenColor(toColor);
                }
            }

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " changes all instances of " + fromText + " to " + toText + " on " , target.getCard(), "."));
            log.info("Game {} - {} changes {} to {} on {}", gameData.id, player.getUsername(), fromText, toText, target.getCard().getName());
        } else {
            // Glamerdye may target a spell still on the stack; record the change so it carries onto the
            // permanent that spell resolves into (CR 613.7). For instants/sorceries it is a no-op.
            StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, ctx.targetId());
            if (targetSpell != null) {
                gameData.spellTextReplacements
                        .computeIfAbsent(ctx.targetId(), k -> new ArrayList<>())
                        .add(new TextReplacement(fromText, toText));
                gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " changes all instances of " + fromText + " to " + toText + " on " , targetSpell.getCard(), "."));
                log.info("Game {} - {} changes {} to {} on spell {}", gameData.id, player.getUsername(), fromText, toText, targetSpell.getCard().getName());
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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

        gameLogService.append(gameData,
                GameLog.playerChoosesForCard(player.getUsername(), cardName, enteredCard));
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, enteredCard.getName(), playerName);
        log.info("Game {} - {} chooses card name \"{}\" for {}", gameData.id, player.getUsername(), cardName, card.getName());

        legendRuleService.checkLegendRule(gameData, controllerId);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Null Chamber: the controller names a card, then their opponent names one, and only then does
     * the enchantment enter carrying both names (CR 614.1c). If the opponent has left the game there
     * is nobody to make the second choice, so the permanent enters with the one name.
     */
    private void handleDualCardNameChosen(GameData gameData, Player player, String cardName,
                                          ChoiceContext.DualCardNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        Card card = ctx.card();
        gameLogService.append(gameData, GameLog.playerChoosesForCard(player.getUsername(), cardName, card));
        log.info("Game {} - {} chooses card name \"{}\" for {}", gameData.id, player.getUsername(), cardName, card.getName());

        UUID opponentId = gameQueryService.getOpponentId(gameData, ctx.controllerId());
        if (ctx.firstChosenName() == null && opponentId != null) {
            playerInputService.beginDualCardNameChoice(gameData,
                    new ChoiceContext.DualCardNameChoice(card, ctx.controllerId(), opponentId, cardName));
            return;
        }

        UUID controllerId = ctx.controllerId();
        Permanent perm = new Permanent(card);
        perm.setChosenName(ctx.firstChosenName() == null ? cardName : ctx.firstChosenName());
        perm.setSecondChosenName(ctx.firstChosenName() == null ? null : cardName);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(perm.getCard(), playerName));
        log.info("Game {} - {} resolves, enters battlefield for {}", gameData.id, perm.getCard().getName(), playerName);

        legendRuleService.checkLegendRule(gameData, controllerId);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " gains " + keywordName + " until end of turn."));
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), keywordName, target.getCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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

        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses \"" + chosen + "\" for " , ctx.sourceCard(), "."));
        inputCompletionService.publishStateAfterInput(gameData);
    }

    /**
     * Hullbreaker Horror: after the "choose up to one" list pick, either finish (NONE) or hand the
     * chosen bounce effect to {@link PermanentChoiceContext.MayAbilityTriggerTarget} for target
     * selection. SPELL mode lists opponent-controlled spells on the stack; PERMANENT mode lists
     * every nonland permanent.
     */
    private void handleHullbreakerHorrorModeChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.HullbreakerHorrorModeChoice ctx) {
        boolean spellMode = ChoiceContext.HullbreakerHorrorModeChoice.SPELL.equals(chosen);
        boolean permanentMode = ChoiceContext.HullbreakerHorrorModeChoice.PERMANENT.equals(chosen);
        boolean noneMode = ChoiceContext.HullbreakerHorrorModeChoice.NONE.equals(chosen);
        if (!spellMode && !permanentMode && !noneMode) {
            throw new IllegalArgumentException("Invalid Hullbreaker Horror mode: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        // The mode choice paused the trigger's resolution after its only effect. Drain the parked
        // entry now (a past-the-end resume): that clears pendingEffectResolutionEntry, releases
        // deferPlayerLossCheck, and runs the post-resolution SBA. Nulling the entry instead would
        // leave the loss check wedged (see InputHandlerEpilogueRatchetTest).
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
        }

        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " chooses \"" + chosen + "\" for ",
                        ctx.sourceCard(), "."));

        if (noneMode) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (spellMode) {
            List<UUID> validSpellCardIds = new ArrayList<>();
            for (StackEntry se : gameData.stack) {
                StackEntryType type = se.getEntryType();
                if (type == StackEntryType.ACTIVATED_ABILITY || type == StackEntryType.TRIGGERED_ABILITY) {
                    continue;
                }
                if (!ctx.controllerId().equals(se.getControllerId())) {
                    validSpellCardIds.add(se.getCard().getId());
                }
            }
            if (validSpellCardIds.isEmpty()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                    ctx.sourceCard(), ctx.controllerId(), List.of(new ReturnTargetSpellToHandEffect())));
            playerInputService.beginAnyTargetChoice(gameData, ctx.controllerId(),
                    validSpellCardIds, List.of(),
                    ctx.sourceCard().getName() + " — Choose target spell you don't control.");
            inputCompletionService.publishStateAfterInput(gameData);
            return;
        }

        // permanent mode
        List<UUID> validPermanents = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!permanent.getCard().hasType(CardType.LAND)) {
                    validPermanents.add(permanent.getId());
                }
            }
        }
        if (validPermanents.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                ctx.sourceCard(), ctx.controllerId(), List.of(ReturnToHandEffect.target())));
        playerInputService.beginPermanentChoice(gameData, ctx.controllerId(), validPermanents,
                ctx.sourceCard().getName() + " — Choose target nonland permanent.");
        inputCompletionService.publishStateAfterInput(gameData);
    }

    /**
     * Quarry Hauler: apply the controller's add/remove decision to the first remaining counter kind
     * on the target, then re-prompt for the next kind (if any) or resume the paused ETB resolution.
     * ADD routes through {@link PermanentCounterSupport#placeCounterOnPermanent} so counter-specific
     * behaviour (+1/+1 triggers, -1/-1 prevention/watchers, saga chapters) is preserved; REMOVE simply
     * decrements. State-based actions are checked once all kinds are done (a lethal -1/-1 or a
     * planeswalker at 0 loyalty resolves only after the whole ability finishes).
     */
    private void handleAdjustCounterKindChoice(GameData gameData, Player player, String choice,
            ChoiceContext.AdjustCounterKindChoice ctx) {
        if (!ChoiceContext.AdjustCounterKindChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid counter adjustment: " + choice);
        }

        gameData.interaction.clearAwaitingInput();

        List<CounterType> remaining = new ArrayList<>(ctx.remainingKinds());
        CounterType kind = remaining.removeFirst();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            if (ChoiceContext.AdjustCounterKindChoice.ADD.equals(choice)) {
                // The paused ETB trigger is the counter source (used for logging / saga chapters).
                StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
                if (sourceEntry != null) {
                    permanentCounterSupport.placeCounterOnPermanent(gameData, sourceEntry, target, kind, 1);
                } else {
                    target.setCounterCount(kind, target.getCounterCount(kind) + 1);
                }
            } else {
                int current = target.getCounterCount(kind);
                if (current > 0) {
                    target.setCounterCount(kind, current - 1);
                    String label = kind.name().toLowerCase().replace('_', ' ');
                    gameLogService.append(gameData, GameLog.textCardText(
                            ctx.sourceCardName() + " removes a " + label + " counter from ", target.getCard(), "."));
                    log.info("Game {} - {} removes a {} counter from {}", gameData.id,
                            ctx.sourceCardName(), kind, target.getCard().getName());
                }
            }
        }

        // Still a kind left (and the target survived) — prompt for it before finishing.
        if (target != null && !remaining.isEmpty()) {
            playerInputService.beginAdjustCounterKindChoice(gameData, ctx.controllerId(), ctx.targetId(),
                    ctx.sourceCardName(), remaining);
            inputCompletionService.publishStateAfterInput(gameData);
            return;
        }

        stateBasedActionService.performStateBasedActions(gameData);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * A modal ("choose one") triggered ability's mode pick, made as the ability resolves. The chosen
     * mode's effects are spliced into the ability's paused resolution (right where the
     * {@link ChooseOneEffect} sat) and resolved in order via the shared pause/resume machinery — so
     * gain-life / lose-life resolve immediately and a surveil mode still queues its "may" prompt.
     * Used by non-targeting modal upkeep triggers such as Etherwrought Page.
     */
    private void handleChooseModeChoice(GameData gameData, Player player, String chosenLabel,
            ChoiceContext.ChooseModeChoice ctx) {
        ChooseOneEffect.ChooseOneOption chosen = ctx.effect().options().stream()
                .filter(o -> o.label().equals(chosenLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid mode: " + chosenLabel));

        gameData.interaction.clearAwaitingInput();

        // Splice the chosen mode's effects into the paused resolution at the ChooseOneEffect's slot
        // so they resolve in card-text order through the same effect loop.
        if (gameData.pendingEffectResolutionEntry != null) {
            gameData.pendingEffectResolutionEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, chosen.effects());
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + chosenLabel + "\" for ", ctx.sourceCard(), "."));
        log.info("Game {} - {} chooses mode \"{}\" for {}", gameData.id, player.getUsername(),
                chosenLabel, ctx.sourceCard().getName());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to reveal for Abundance."));
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

        gameLogService.append(gameData,
                appendCards(GameLog.builder().text(playerName + " reveals "), revealed)
                        .text(" for Abundance.")
                        .build());

        List<Card> toBottom = new ArrayList<>(revealed);
        if (chosenCard != null) {
            gameData.addCardToHand(playerId, chosenCard);
            toBottom.remove(chosenCard);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", chosenCard, " into their hand."));
        } else {
            String missingKind = chooseLand ? "land" : "nonland";
            gameLogService.append(gameData, GameLog.text(playerName + " reveals no " + missingKind + " card for Abundance."));
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
        if (gameData.interaction.isAwaitingInput()) {
            inputCompletionService.publishStateAfterInput(gameData);
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleProtectionColorChoice(GameData gameData, Player player, String chosenValue, ChoiceContext.ProtectionColorChoice ctx) {
        // Parse before touching interaction state, as the dispatcher's own fallback does: an
        // unparseable answer must leave the prompt standing, since clearing it first destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry.
        CardColor chosenColor = "ARTIFACT".equals(chosenValue) ? null : CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        for (UUID targetId : ctx.targetIds()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            if (chosenColor == null) {
                target.getProtectionFromCardTypes().add(CardType.ARTIFACT);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " gains protection from artifacts until end of turn."));
                log.info("Game {} - {} gains protection from artifacts until end of turn", gameData.id, target.getCard().getName());
            } else {
                CardColor color = chosenColor;
                target.getProtectionFromColorsUntilEndOfTurn().add(color);
                String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " gains protection from " + colorName.toLowerCase() + " until end of turn."));
                log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), colorName.toLowerCase());
            }
        }

        // CR 704.5n/704.5q — the new protection can make an attached aura or equipment illegal
        stateBasedActionService.performStateBasedActions(gameData);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleMassProtectionColorChoice(GameData gameData, Player player, String chosenValue,
            ChoiceContext.MassProtectionColorChoice ctx) {
        // Parse before touching interaction state, as the dispatcher's own fallback does: an
        // unparseable answer must leave the prompt standing, since clearing it first destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry.
        CardColor color = CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

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
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} and their permanents gain protection from {} until end of turn",
                gameData.id, playerName, colorName.toLowerCase());

        // CR 704.5n/704.5q — the new protection can make attached auras or equipment illegal
        stateBasedActionService.performStateBasedActions(gameData);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleColorSetChoice(GameData gameData, String chosenValue, ChoiceContext.ColorSetChoice ctx) {
        // Parse before touching interaction state, as the dispatcher's own fallback does: an
        // unparseable answer must leave the prompt standing, since clearing it first destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry.
        CardColor chosenColor = CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            CardColor color = chosenColor;

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
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " becomes " + colorName + " until end of turn."));
            log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            gameLogService.append(gameData, GameLog.text(controllerName + " chooses " + colorLabel.toLowerCase()
                    + ". " + targetName + " reveals an empty hand."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(controllerName + " chooses " + colorLabel.toLowerCase()
                            + ". " + targetName + " reveals their hand: "), hand)
                            .text(".")
                            .build());
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
            gameLogService.append(gameData, GameLog.text(targetName + " discards " + toDiscard.size()
                    + " " + colorLabel.toLowerCase() + " card" + (toDiscard.size() != 1 ? "s" : "") + "."));
            log.info("Game {} - {} discards {} {} card(s) to Persecute-style effect",
                    gameData.id, targetName, toDiscard.size(), colorLabel.toLowerCase());
        } else {
            gameLogService.append(gameData, GameLog.text(targetName + " has no " + colorLabel.toLowerCase()
                    + " cards to discard."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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

        gameLogService.append(gameData, GameLog.text(controllerName + " chooses " + colorLabel.toLowerCase()
                + ". " + targetName + " exiles the top " + toExile + " card" + (toExile != 1 ? "s" : "")
                + " of their library."));
        log.info("Game {} - Oona: {} exiles {} card(s); {} of chosen colour {}",
                gameData.id, targetName, toExile, matches, colorLabel.toLowerCase());

        if (matches > 0) {
            permanentControlSupport.applyCreateToken(gameData, controllerId, ctx.tokenTemplate(), matches, ctx.sourceSetCode());
            gameLogService.append(gameData, GameLog.text(controllerName + " creates " + matches
                    + " Faerie Rogue token" + (matches != 1 ? "s" : "") + "."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
        gameLogService.append(gameData, GameLog.text(controllerName + " chooses "
                + colorLabel.toLowerCase() + "."));
        log.info("Game {} - Rith: chosen colour {}, {} matching permanent(s)",
                gameData.id, colorLabel.toLowerCase(), count);

        if (count > 0) {
            permanentControlSupport.applyCreateToken(gameData, controllerId, ctx.tokenTemplate(), count, ctx.sourceSetCode());
            gameLogService.append(gameData, GameLog.text(controllerName + " creates " + count
                    + " Saproling token" + (count != 1 ? "s" : "") + "."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Hall of Gemstone: record the chosen color as the color every land produces for the rest of the
     * turn. Cleared by {@code TurnCleanupService}.
     */
    private void handleAllLandsProduceChosenColorChoice(GameData gameData, String chosenValue,
            ChoiceContext.AllLandsProduceChosenColorChoice ctx) {
        ManaColor color = ManaColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        gameData.allLandsFixedManaColorThisTurn = color;

        String playerName = gameData.playerIdToName.get(ctx.playerId());
        gameLogService.append(gameData, GameLog.text(playerName + " chooses "
                + color.name().toLowerCase() + "; lands produce that color this turn."));
        log.info("Game {} - Hall of Gemstone: lands produce {} this turn", gameData.id, color);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Prismwake Merrow: accumulate the controller's color picks. Each color adds to the running set
     * and re-prompts (with a "DONE" option); "DONE", a repeated color, or all five colors finalizes
     * the choice — the target then becomes those colors until end of turn.
     */
    private void handleBecomeChosenColorsChoice(GameData gameData, Player player, String chosenValue,
            ChoiceContext.BecomeChosenColorsChoice ctx) {
        // Parse before touching interaction state, as the dispatcher's own fallback does: an
        // unparseable answer must leave the prompt standing, since clearing it first destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry.
        CardColor chosenColor = "DONE".equals(chosenValue) ? null : CardColor.valueOf(chosenValue);

        gameData.interaction.clearAwaitingInput();

        List<CardColor> chosen = new ArrayList<>(ctx.chosen());
        if (chosenColor != null) {
            CardColor color = chosenColor;
            // A repeated color (e.g. a naive AI re-picking the same color) ends the choice rather
            // than looping forever; otherwise add it and, if fewer than five are chosen, re-prompt.
            if (!chosen.contains(color)) {
                chosen.add(color);
                if (chosen.size() < CardColor.values().length) {
                    playerInputService.beginBecomeChosenColorsChoice(gameData, player.getId(),
                            ctx.targetId(), ctx.sourceCardName(), chosen, ctx.duration());
                    inputCompletionService.publishStateAfterInput(gameData);
                    return;
                }
            }
        }

        applyBecomeChosenColors(gameData, ctx, player.getId(), chosen);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void applyBecomeChosenColors(GameData gameData, ChoiceContext.BecomeChosenColorsChoice ctx,
            UUID controllerId, List<CardColor> colors) {
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target == null || colors.isEmpty()) {
            return;
        }

        Set<CardColor> colorSet = new LinkedHashSet<>(colors);

        // CR 613 layer engine: "becomes [colors]" is a floating layer-5 color-setting effect with
        // its own timestamp. Duration is UNTIL_END_OF_TURN (Prismwake Merrow) or PERMANENT (Shyft).
        // The legacy transient fields are still written for direct Permanent.getEffectiveColor
        // callers; the layered pass replays the setter.
        target.getTransientColors().clear();
        target.getTransientColors().addAll(colorSet);
        target.setColorOverridden(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                ctx.sourceCardName(), null, controllerId,
                new BecomeChosenColorsUntilEndOfTurnEffect(colorSet),
                target.getId(), null, null, ctx.duration(), 0));

        String colorList = colorSet.stream()
                .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                .reduce((a, b) -> a + " and " + b).orElse("");
        String durationSuffix = ctx.duration() == EffectDuration.UNTIL_END_OF_TURN
                ? " until end of turn." : ".";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " becomes " + colorList + durationSuffix));
        log.info("Game {} - {} becomes {}{}", gameData.id, target.getCard().getName(), colorList,
                durationSuffix);
    }

    /**
     * Torment of Hailfire: the affected opponent picked one of the pruned penalty options. Record the
     * choice on {@link GameData#torment} and resume the paused spell, which re-runs
     * {@code TormentOfHailfireEffectHandler} to apply the choice (a sacrifice/discard sub-choice, or
     * an inline life loss) and advance to the next opponent.
     */
    private void handleTormentPenaltyChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.TormentPenaltyChoice ctx) {
        PendingInteraction.ColorChoice active =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (active == null || !active.options().contains(chosen)) {
            throw new IllegalArgumentException("Invalid Torment of Hailfire choice: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.torment.chosenMode = chosen;

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + chosen + "\" for " + ctx.sourceCardName() + "."));
        log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), chosen, ctx.sourceCardName());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Forbidden Ritual: the targeted opponent picked one of the pruned penalty options. Record the
     * choice on {@link GameData#forbiddenRitual} and resume the paused spell, which re-runs
     * {@code ForbiddenRitualEffectHandler} to apply the choice.
     */
    private void handleForbiddenRitualPenaltyChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.ForbiddenRitualPenaltyChoice ctx) {
        PendingInteraction.ColorChoice active =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (active == null || !active.options().contains(chosen)) {
            throw new IllegalArgumentException("Invalid Forbidden Ritual choice: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.forbiddenRitual.chosenMode = chosen;

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + chosen + "\" for " + ctx.sourceCardName() + "."));
        log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), chosen, ctx.sourceCardName());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Oath of Lim-Dûl: the controller picked sacrifice-another-permanent or discard for one life
     * point. Record the choice on {@link GameData#torment} and resume so the effect handler applies
     * it and advances to the next life point.
     */
    private void handleOathOfLimDulPenaltyChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.OathOfLimDulPenaltyChoice ctx) {
        PendingInteraction.ColorChoice active =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (active == null || !active.options().contains(chosen)) {
            throw new IllegalArgumentException("Invalid Oath of Lim-Dûl choice: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.torment.chosenMode = chosen;

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + chosen + "\" for " + ctx.sourceCardName() + "."));
        log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), chosen, ctx.sourceCardName());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Winter's Chill: the creature's controller picked pay {2}, pay {1}, or pay nothing. Record the
     * choice on {@link GameData#wintersChill} and resume so {@code WintersChillEffectHandler} applies
     * it and advances to the next target.
     */
    private void handleWintersChillPaymentChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.WintersChillPaymentChoice ctx) {
        PendingInteraction.ColorChoice active =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (active == null || !active.options().contains(chosen)) {
            throw new IllegalArgumentException("Invalid Winter's Chill choice: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.wintersChill.chosenMode = chosen;

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + chosen + "\" for " + ctx.sourceCardName() + "."));
        log.info("Game {} - {} chooses {} for Winter's Chill target {}",
                gameData.id, player.getUsername(), chosen, ctx.targetPermanentId());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Forgotten Lore: the controller chose whether to pay {G} and repeat the process. Record the
     * choice on {@link GameData#forgottenLore} and resume so {@code ForgottenLoreEffectHandler}
     * either charges the mana and prompts the opponent again, or ends the loop.
     */
    private void handleForgottenLorePaymentChoice(GameData gameData, Player player, String chosen,
            ChoiceContext.ForgottenLorePaymentChoice ctx) {
        PendingInteraction.ColorChoice active =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (active == null || !active.options().contains(chosen)) {
            throw new IllegalArgumentException("Invalid Forgotten Lore choice: " + chosen);
        }

        gameData.interaction.clearAwaitingInput();
        gameData.forgottenLore.chosenMode = chosen;

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + chosen + "\" for " + ctx.sourceCardName() + "."));
        log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), chosen, ctx.sourceCardName());

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleSubtypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.SubtypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenSubtype(subtype);

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses " + subtype.getDisplayName() + " for " , perm.getCard(), "."));
            log.info("Game {} - {} chooses creature type {} for {}", gameData.id, player.getUsername(), subtype, perm.getCard().getName());

            // The subtype choice deferred the permanent's ETB triggers (they were skipped while input
            // was pending). Now that the type is chosen, process them — e.g. Brass Herald's "reveal the
            // top four cards" trigger, which reads the chosen type from the permanent.
            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), perm.getCard(), null, true);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleSpellCreatureTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.SpellCreatureTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.chosenSpellSubtype = subtype;
        gameData.interaction.clearAwaitingInput();

        String logEntry = player.getUsername() + " chooses " + subtype.getDisplayName() + ".";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chooses creature type {} for a spell", gameData.id, player.getUsername(), subtype);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleManaValueParityChoice(GameData gameData, Player player, String parityName, ChoiceContext.ManaValueParityChoice ctx) {
        com.github.laxika.magicalvibes.model.ManaValueParity parity =
                com.github.laxika.magicalvibes.model.ManaValueParity.valueOf(parityName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenManaValueParity(parity);

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses " + parityName.toLowerCase() + " for " , perm.getCard(), "."));
            log.info("Game {} - {} chooses {} for {}", gameData.id, player.getUsername(), parityName.toLowerCase(), perm.getCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleNumberChoice(GameData gameData, Player player, String numberName, ChoiceContext.NumberChoice ctx) {
        int chosen = Integer.parseInt(numberName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenNumber(chosen);

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses " + chosen + " for " , perm.getCard(), "."));
            log.info("Game {} - {} chooses number {} for {}", gameData.id, player.getUsername(), chosen, perm.getCard().getName());
        }

        // Resumes the paused upkeep may-ability resolution when present; otherwise auto-passes
        // (the "as this enters" ETB choice, which has no pending stack-effect resolution).
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handlePayAnyAmountOfLifeAsEnters(GameData gameData, Player player, String numberName,
                                                  ChoiceContext.PayAnyAmountOfLifeAsEnters ctx) {
        int paid = Integer.parseInt(numberName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            perm.setChosenNumber(paid);
        }

        if (paid > 0) {
            lifeSupport.applyLifeLoss(gameData, player.getId(), paid, ctx.card().getName());
        }

        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " pays " + paid + " life for ", ctx.card(), "."));
        log.info("Game {} - {} pays {} life for {}", gameData.id, player.getUsername(), paid, ctx.card().getName());

        // The life payment deferred the permanent's ETB triggers (skipped while input was pending).
        battlefieldEntryService.processCreatureETBEffects(gameData, ctx.controllerId(), ctx.card(),
                ctx.targetId(), ctx.wasCastFromHand(), ctx.etbMode(), ctx.kicked());

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
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

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " removes " + removed + " "
                    + ctx.counterType().name().toLowerCase() + " counter(s) from ", perm.getCard(),
                    " and adds " + mana + " " + ctx.color().getCode() + "."));
            log.info("Game {} - {} removes {} {} counters and adds {} {} mana", gameData.id,
                    player.getUsername(), removed, ctx.counterType(), mana, ctx.color());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            gameData.sourceCreatedTokens
                    .computeIfAbsent(ctx.permanentId(), k -> ConcurrentHashMap.<UUID>newKeySet())
                    .addAll(createdIds);

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " removes " + removed + " +1/+1 counter"
                    + (removed == 1 ? "" : "s") + " from ", source.getCard(),
                    " to create " + removed + " Tetravite token" + (removed == 1 ? "" : "s") + "."));
            log.info("Game {} - {} removes {} +1/+1 counters from {} to create {} Tetravite tokens",
                    gameData.id, player.getUsername(), removed, source.getCard().getName(), removed);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses a " + form.power() + "/" + form.toughness()
                    + (form.keyword() != null ? " " + form.keyword().name().toLowerCase() : "")
                    + " shape for ", perm.getCard(), "."));
            log.info("Game {} - {} chooses shape {} for {}", gameData.id, player.getUsername(), form, perm.getCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleBasicLandTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.BasicLandTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent perm = gameQueryService.findPermanentById(gameData, ctx.permanentId());
        if (perm != null) {
            if (ctx.isSecondChoice()) {
                perm.setSecondChosenSubtype(subtype);
            } else {
                perm.setChosenSubtype(subtype);
            }

            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses " + subtype.getDisplayName() + " for " , perm.getCard(), "."));
            log.info("Game {} - {} chooses basic land type {} for {} (second={})",
                    gameData.id, player.getUsername(), subtype, perm.getCard().getName(), ctx.isSecondChoice());
        }

        if (!ctx.isSecondChoice() && ctx.chainSecondAfter()) {
            playerInputService.beginBasicLandTypeChoice(
                    gameData, player.getId(), ctx.permanentId(), true, false, ctx.allowedTypes());
            inputCompletionService.publishStateAfterInput(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleAddBasicLandTypeChoice(GameData gameData, Player player, String subtypeName, ChoiceContext.AddBasicLandTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent targetLand = gameQueryService.findPermanentById(gameData, ctx.targetLandId());
        if (targetLand != null) {
            GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(targetLand, subtype, ctx.duration(), ctx.replacing());

            String durationText = switch (ctx.duration()) {
                case UNTIL_END_OF_TURN -> " until end of turn";
                case UNTIL_CONTROLLERS_NEXT_UNTAP_STEP -> " until its controller's next untap step";
                default -> "";
            };
            String typeSuffix = ctx.replacing() ? "" : " in addition to its other types";
            gameLogService.append(gameData, GameLog.cardThen(targetLand.getCard(),
                    " becomes a " + subtype.getDisplayName() + typeSuffix + durationText + "."));
            log.info("Game {} - {} becomes a {} (replacing={})", gameData.id, targetLand.getCard().getName(), subtype, ctx.replacing());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Gives the target creature snow landwalk of the chosen type until end of turn (Barbarian
     * Guides): it can't be blocked while the defending player controls a land that is both snow
     * and of that type (CR 702.14c).
     */
    private void handleSnowLandwalkGrantChoice(GameData gameData, Player player, String subtypeName,
                                              ChoiceContext.SnowLandwalkGrantChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target != null) {
            target.getUnblockableIfDefenderControlsUntilEndOfTurn().add(new PermanentAllOfPredicate(List.of(
                    new PermanentHasSubtypePredicate(subtype),
                    new PermanentHasSupertypePredicate(CardSupertype.SNOW))));

            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " gains snow " + subtype.getDisplayName().toLowerCase() + "walk until end of turn."));
            log.info("Game {} - {} gains snow {}walk until end of turn",
                    gameData.id, target.getCard().getName(), subtype);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - Each land {} controls becomes a {} until end of turn", gameData.id, playerName, subtype);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleLandsOfTypeBecomeBasicTypeChoice(GameData gameData, Player player, String subtypeName,
                                                        ChoiceContext.LandsOfTypeBecomeBasicTypeChoice ctx) {
        CardSubtype subtype = CardSubtype.valueOf(subtypeName);

        gameData.interaction.clearAwaitingInput();

        if (ctx.fromType() == null) {
            playerInputService.beginLandsOfTypeBecomeBasicTypeChoice(gameData, player.getId(), subtype);
            inputCompletionService.publishStateAfterInput(gameData);
            return;
        }

        CardSubtype fromType = ctx.fromType();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!permanent.getCard().hasType(CardType.LAND)) {
                    continue;
                }
                if (!gameQueryService.effectiveBasicLandTypes(gameData, permanent).contains(fromType)) {
                    continue;
                }
                GrantBasicLandTypeToTargetEffectHandler.applyBasicLandType(
                        permanent, subtype, EffectDuration.UNTIL_END_OF_TURN, true);
            }
        }

        String logEntry = "Each land of type " + fromType.getDisplayName() + " becomes a "
                + subtype.getDisplayName() + " until end of turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - Each land of type {} becomes a {} until end of turn",
                gameData.id, fromType, subtype);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : toReturn) {
                    graveyard.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                    gameData.addCardToHand(controllerId, card);
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }

            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(playerName + " chooses " + chosenType.getDisplayName()
                            + " and returns "), toReturn)
                            .text(" to hand.")
                            .build());
            log.info("Game {} - {} chooses {} and returns {} card(s) from graveyard to hand",
                    gameData.id, playerName, chosenType.getDisplayName(), toReturn.size());
        } else {
            String logEntry = playerName + " chooses " + chosenType.getDisplayName()
                    + " but has no " + chosenType.getDisplayName().toLowerCase() + " cards in graveyard.";
            gameLogService.append(gameData, GameLog.text(logEntry));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chooses {} for Storage Matrix untap", gameData.id, playerName, typeName);

        turnProgressionService.resumeStorageMatrixUntap(gameData, ctx.playerId(), restrict);
    }

    private void handleTeferisRealmTypeChoice(GameData gameData, Player player, String typeName,
                                              ChoiceContext.TeferisRealmTypeChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        String display = "NON_AURA_ENCHANTMENT".equals(typeName) ? "non-Aura enchantment" : typeName.toLowerCase();
        String playerName = gameData.playerIdToName.get(ctx.playerId());
        gameLogService.append(gameData, GameLog.text(playerName + " chooses " + display + "."));
        log.info("Game {} - {} chooses {} for Teferi's Realm", gameData.id, playerName, typeName);

        phaseOutChosenTypeSupport.phaseOutChosenType(gameData, ctx.sourceCard(), typeName);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleEachPlayerCardNameRevealChoice(GameData gameData, Player player, String cardName,
                                                      ChoiceContext.EachPlayerCardNameRevealChoice ctx) {
        // Store this player's chosen name
        Map<UUID, String> updatedNames = new LinkedHashMap<>(ctx.chosenNames());
        updatedNames.put(player.getId(), cardName);

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameLogService.append(gameData, GameLog.text(choiceLog));
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
            inputCompletionService.publishStateAfterInput(gameData);
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
                gameLogService.append(gameData, GameLog.text(logEntry));
                continue;
            }

            Card topCard = deck.removeFirst();
            gameLogService.append(gameData, GameLog.textCardText(playerName + " reveals " , topCard, "."));

            if (topCard.getName().equals(chosenName)) {
                gameData.addCardToHand(pid, topCard);
                gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , topCard, " into their hand."));
                log.info("Game {} - {} guessed correctly, {} goes to hand", gameData.id, playerName, topCard.getName());
            } else {
                deck.add(topCard);
                gameLogService.append(gameData, GameLog.textCardText(playerName + " puts " , topCard, " on the bottom of their library."));
                log.info("Game {} - {} guessed wrong, {} goes to bottom", gameData.id, playerName, topCard.getName());
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleNameCardMillGainLifeChoice(GameData gameData, Player player, String cardName,
                                                  ChoiceContext.NameCardMillGainLifeChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameLogService.append(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" (name/mill/gain life)",
                gameData.id, player.getUsername(), cardName);

        Card matched = millAndMatchingNamedCard(gameData, ctx.targetPlayerId(), cardName);
        if (matched != null) {
            int manaValue = matched.getManaValue();
            lifeSupport.applyGainLife(gameData, ctx.controllerId(), manaValue);
            String controllerName = gameData.playerIdToName.get(ctx.controllerId());
            String lifeLog = controllerName + " gains " + manaValue + " life.";
            gameLogService.append(gameData, GameLog.text(lifeLog));
            log.info("Game {} - {} milled the named card {}, {} gains {} life",
                    gameData.id, gameData.playerIdToName.get(ctx.targetPlayerId()),
                    matched.getName(), controllerName, manaValue);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleNameCardMillDrawChoice(GameData gameData, Player player, String cardName,
                                              ChoiceContext.NameCardMillDrawChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameLogService.append(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" (name/mill/draw)",
                gameData.id, player.getUsername(), cardName);

        Card matched = millAndMatchingNamedCard(gameData, ctx.targetPlayerId(), cardName);
        if (matched != null) {
            playerInteractionSupport.applyDrawCards(gameData, ctx.controllerId(), 1);
            String controllerName = gameData.playerIdToName.get(ctx.controllerId());
            log.info("Game {} - {} milled the named card {}, {} draws a card",
                    gameData.id, gameData.playerIdToName.get(ctx.targetPlayerId()),
                    matched.getName(), controllerName);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Mills one card from {@code targetPlayerId}. Returns the milled card if its name matches
     * {@code cardName} and it actually reached the graveyard; otherwise {@code null}.
     */
    private Card millAndMatchingNamedCard(GameData gameData, UUID targetPlayerId, String cardName) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        Card topCard = (deck != null && !deck.isEmpty()) ? deck.getFirst() : null;

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);

        // "If a card with the chosen name was milled this way" — the card must have both matched the
        // chosen name and actually reached the graveyard (a replacement effect could redirect it).
        if (topCard != null && topCard.getName().equals(cardName)) {
            List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
            boolean reachedGraveyard = graveyard != null
                    && graveyard.stream().anyMatch(c -> c.getId().equals(topCard.getId()));
            if (reachedGraveyard) {
                return topCard;
            }
        }
        return null;
    }

    private void handleOpponentsCantCastNamedSpellsUntilNextTurnChoice(GameData gameData, Player player,
                                                                       String cardName,
                                                                       ChoiceContext.OpponentsCantCastNamedSpellsUntilNextTurnChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        UUID controllerId = ctx.controllerId();
        gameData.opponentsCantCastNamedSpellsUntilControllerNextTurn
                .computeIfAbsent(controllerId, k -> ConcurrentHashMap.newKeySet())
                .add(cardName);

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + cardName + "\". Opponents can't cast spells named "
                        + cardName + " until " + gameData.playerIdToName.get(controllerId) + "'s next turn."));
        log.info("Game {} - {} chooses card name \"{}\" (opponents can't cast until next turn)",
                gameData.id, player.getUsername(), cardName);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleChooseNameExileTopRevealUntilNamedChoice(GameData gameData, Player player,
                                                                String cardName,
                                                                ChoiceContext.ChooseNameExileTopRevealUntilNamedChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        UUID controllerId = ctx.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " chooses \"" + cardName + "\"."));
        log.info("Game {} - {} chooses card name \"{}\" (exile-top/reveal-until-named)",
                gameData.id, player.getUsername(), cardName);

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Exile the top N cards (or fewer if the library is smaller).
        int toExile = Math.min(ctx.topExileCount(), deck.size());
        List<Card> initialExile = new ArrayList<>(toExile);
        for (int i = 0; i < toExile; i++) {
            initialExile.add(deck.removeFirst());
        }
        for (Card card : initialExile) {
            gameData.addToExile(controllerId, card);
        }
        if (toExile > 0) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " exiles the top " + toExile + " card"
                            + (toExile == 1 ? "" : "s") + " of their library."));
        }

        // Reveal until the named card (or the library empties).
        List<Card> revealed = new ArrayList<>();
        Card found = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (card.getName().equals(cardName)) {
                found = card;
                break;
            }
        }

        if (!revealed.isEmpty()) {
            String revealedNames = revealed.stream().map(Card::getName)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " reveals " + revealedNames + "."));
        }

        if (found != null) {
            revealed.remove(found);
            gameData.addCardToHand(controllerId, found);
            gameLogService.append(gameData, GameLog.textCardText(
                    controllerName + " puts ", found, " into their hand."));
            for (Card card : revealed) {
                gameData.addToExile(controllerId, card);
            }
            if (!revealed.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        controllerName + " exiles the other revealed cards."));
            }
        } else {
            for (Card card : revealed) {
                gameData.addToExile(controllerId, card);
            }
            if (revealed.isEmpty() && initialExile.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        controllerName + "'s library is empty."));
            } else {
                gameLogService.append(gameData, GameLog.text(
                        controllerName + " does not reveal a card named \"" + cardName
                                + "\" — remaining revealed cards are exiled."));
            }
        }

        log.info("Game {} - {} Demonic Consultation dig: initialExile={}, revealed={}, found={}",
                gameData.id, controllerName, initialExile.size(), revealed.size(),
                found != null ? found.getName() : "none");

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleTargetPlayerNameCardRevealTopChoice(GameData gameData, Player player, String cardName,
                                                           ChoiceContext.TargetPlayerNameCardRevealTopChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        UUID targetPlayerId = ctx.targetPlayerId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = player.getUsername() + " chooses \"" + cardName + "\".";
        gameLogService.append(gameData, GameLog.text(choiceLog));
        log.info("Game {} - {} chooses card name \"{}\" (name-card-reveal-top)",
                gameData.id, player.getUsername(), cardName);

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + "'s library is empty."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.textCardText(targetName + " reveals ", topCard, "."));

        if (topCard.getName().equals(cardName)) {
            deck.removeFirst();
            gameData.addCardToHand(targetPlayerId, topCard);
            gameLogService.append(gameData, GameLog.textCardText(targetName + " puts ", topCard, " into their hand."));
            log.info("Game {} - {} named correctly, {} goes to hand", gameData.id, targetName, topCard.getName());
        } else {
            graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);
            gameLogService.append(gameData, GameLog.textCardText(targetName + " puts ", topCard, " into their graveyard."));
            dealRevealMissDamage(gameData, ctx, targetPlayerId);
            log.info("Game {} - {} named incorrectly, {} goes to graveyard", gameData.id, targetName, topCard.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
        gameLogService.append(gameData, GameLog.text(choiceLog));
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
            gameLogService.append(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} found 0 cards named \"{}\" in {}'s zones", gameData.id, controllerName, cardName, targetName);

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Present matching cards for "any number" selection
        playerInputService.beginMultiZoneExileChoice(gameData, controllerId, matchingCards, targetPlayerId, cardName);
        inputCompletionService.publishStateAfterInput(gameData);
    }

    /**
     * Thought Hemorrhage: the controller named a card; the target player reveals their hand, the
     * source deals {@code damagePerCard} damage per copy revealed from that hand, then every copy in
     * their hand, graveyard, and library is exiled (mandatory) and they shuffle.
     */
    private void handleRevealHandDamageAndExileByNameChoice(GameData gameData, String cardName,
            ChoiceContext.RevealHandDamageAndExileByNameChoice ctx) {
        gameData.interaction.clearAwaitingInput();

        UUID targetPlayerId = ctx.targetPlayerId();
        UUID controllerId = ctx.controllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        gameLogService.append(gameData, GameLog.text(controllerName + " chooses \"" + cardName + "\"."));
        log.info("Game {} - {} chooses card name \"{}\" (reveal hand, damage, exile)", gameData.id, controllerName, cardName);

        // Target player reveals their hand.
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + " reveals an empty hand."));
        } else {
            gameLogService.append(gameData,
                    appendCards(GameLog.builder().text(targetName + " reveals their hand: "), hand).text(".").build());
        }

        // Damage = damagePerCard for each copy with the chosen name revealed from hand this way.
        long copiesInHand = hand == null ? 0 : hand.stream().filter(c -> c.getName().equals(cardName)).count();
        int damage = (int) (copiesInHand * ctx.damagePerCard());
        if (damage > 0) {
            StackEntry damageEntry = new StackEntry(
                    StackEntryType.SORCERY_SPELL, ctx.sourceCard(), controllerId,
                    ctx.sourceCard().getName(), List.of(), targetPlayerId, (UUID) null);
            damageSupport.dealDamageToPlayer(gameData, damageEntry, targetPlayerId, damage);
        }

        // Exile every copy from the target's hand, graveyard, and library (mandatory, no choice).
        int exiledCount = 0;

        if (hand != null) {
            List<Card> toExile = hand.stream().filter(c -> c.getName().equals(cardName)).toList();
            hand.removeAll(toExile);
            toExile.forEach(card -> gameData.addToExile(targetPlayerId, card));
            exiledCount += toExile.size();
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            List<Card> toExile = graveyard.stream().filter(c -> c.getName().equals(cardName)).toList();
            graveyard.removeAll(toExile);
            toExile.forEach(card -> gameData.addToExile(targetPlayerId, card));
            if (!toExile.isEmpty()) {
                graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
            }
            exiledCount += toExile.size();
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library != null) {
            List<Card> toExile = library.stream().filter(c -> c.getName().equals(cardName)).toList();
            library.removeAll(toExile);
            toExile.forEach(card -> gameData.addToExile(targetPlayerId, card));
            exiledCount += toExile.size();
            Collections.shuffle(library);
        }

        String exileLog = controllerName + " exiles " + exiledCount + " card" + (exiledCount != 1 ? "s" : "")
                + " named \"" + cardName + "\" from " + targetName + "'s hand, graveyard, and library. "
                + targetName + " shuffles their library.";
        gameLogService.append(gameData, GameLog.text(exileLog));
        log.info("Game {} - {} exiled {} card(s) named \"{}\" from {}'s zones and dealt {} damage",
                gameData.id, controllerName, exiledCount, cardName, targetName, damage);

        stateBasedActionService.performStateBasedActions(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleSphinxAmbassadorNameChoice(GameData gameData, Player player, String cardName, ChoiceContext.SphinxAmbassadorNameChoice ctx) {
        // Validate before touching interaction state: clearing first and then throwing destroys the
        // only thing that would resume the entry parked in pendingEffectResolutionEntry.
        PendingSphinxAmbassadorChoice pending = gameData.peekPendingInteraction(PendingSphinxAmbassadorChoice.class);
        if (pending == null || pending.selectedCard() == null) {
            throw new IllegalStateException("No pending Sphinx Ambassador choice");
        }

        gameData.interaction.clearAwaitingInput();

        Card selectedCard = pending.selectedCard();
        UUID controllerId = pending.controllerId();
        UUID targetPlayerId = pending.targetPlayerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        String choiceLog = targetName + " chooses \"" + cardName + "\".";
        gameLogService.append(gameData, GameLog.text(choiceLog));
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
            gameLogService.append(gameData, GameLog.textCardText("The conditions for ", pending.sourceCard(),
                    " are not met. " + targetName + "'s library is shuffled."));
            log.info("Game {} - Sphinx Ambassador: selected card does not match conditions (creature={}, nameMatch={})",
                    gameData.id, isCreature, !nameDoesNotMatch);

            gameData.playerDecks.get(targetPlayerId).add(selectedCard);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameData.clearPendingInteractions(PendingSphinxAmbassadorChoice.class);

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
        gameLogService.append(gameData, GameLog.text(exileLog));
        log.info("Game {} - {} exiled {} card(s) named \"{}\" from {}'s zones",
                gameData.id, controllerName, exiledCount, cardName, targetName);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder = builder.text(", ");
            }
            builder = builder.card(cards.get(i));
        }
        return builder;
    }
}


