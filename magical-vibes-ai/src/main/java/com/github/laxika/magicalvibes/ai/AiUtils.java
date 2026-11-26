package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardDrawingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared static utility methods for AI decision classes.
 */
public final class AiUtils {

    private AiUtils() {}

    /** Who a card draw gives cards to, as the AI scores it ({@link #drawRecipients}). */
    public enum DrawRecipients {
        /** Only the drawing effect's controller: the AI's own card advantage. */
        CONTROLLER,
        /** Only the controller's opponents: a cost to the AI. */
        OPPONENTS,
        /** Every player alike: no card advantage for anyone. */
        EACH_PLAYER,
        /** A target player, chosen as the spell or ability goes on the stack. */
        TARGET_PLAYER,
        /** A player the event or the resolution names (the triggering, defending or a chosen player): not known ahead. */
        OTHER
    }

    /**
     * Who {@code draw} makes draw: the controller ({@code you()}, the source's controller), the opponents
     * ({@code eachOpponent()}, the opponents of you), every player ({@code eachPlayer()}), a target player, or a player
     * only the resolution knows. Only {@link DrawRecipients#CONTROLLER} is the AI's own card advantage.
     */
    public static DrawRecipients drawRecipients(CardDrawingEffect draw) {
        PlayerSubject player = draw.drawingPlayer();
        if (player instanceof PlayerSubject.You
                || player instanceof PlayerSubject.ControllerOf controller
                && controller.permanent() instanceof PermanentSubject.Source) {
            return DrawRecipients.CONTROLLER;
        }
        if (player instanceof PlayerSubject.Each each) {
            return each.relation() == PlayerSubject.Relation.OPPONENTS
                    ? DrawRecipients.OPPONENTS : DrawRecipients.EACH_PLAYER;
        }
        if (player instanceof PlayerSubject.OpponentsOf opponents && opponents.player() instanceof PlayerSubject.You) {
            return DrawRecipients.OPPONENTS;
        }
        return player instanceof PlayerSubject.TargetPlayer ? DrawRecipients.TARGET_PLAYER : DrawRecipients.OTHER;
    }

    /** Whether {@code effect} is a card draw for its controller alone: the AI's own card advantage. */
    public static boolean drawsForController(CardEffect effect) {
        return effect instanceof CardDrawingEffect draw && drawRecipients(draw) == DrawRecipients.CONTROLLER;
    }

    /**
     * {@code effects} with the effects of each delayed ability about the target they make ("this creature deals 2
     * damage to that creature at end of combat", Dwarven Sea Clan) after it: what will happen to the target, read as
     * what the ability does to it.
     */
    public static List<CardEffect> withEffectsDelayedAboutTheTarget(List<? extends CardEffect> effects) {
        List<CardEffect> all = new ArrayList<>();
        for (CardEffect effect : effects) {
            all.add(effect);
            if (effect instanceof RegisterDelayedStepTriggerEffect delayed && delayed.permanent() != null
                    && delayed.permanent().declaresTarget()) {
                all.addAll(delayed.effects());
            }
        }
        return all;
    }

    /**
     * Whether the card's target filter contains a {@link PermanentManaValueEqualsXPredicate}, so
     * the announced X must equal the chosen target's mana value rather than being free to pick
     * (Entrancing Melody, Detonate). Every AI X chooser has to special-case these, including the
     * MCTS simulator's action enumeration, which is why this lives here rather than on one engine.
     */
    public static boolean hasManaValueEqualsXTarget(Card card) {
        TargetFilter filter = card.getTargetFilter();
        return filter instanceof PermanentPredicateTargetFilter pf
                && containsManaValueEqualsXPredicate(pf.predicate());
    }

    private static boolean containsManaValueEqualsXPredicate(PermanentPredicate predicate) {
        if (predicate instanceof PermanentManaValueEqualsXPredicate) {
            return true;
        }
        return predicate instanceof PermanentAllOfPredicate allOf
                && allOf.predicates().stream().anyMatch(AiUtils::containsManaValueEqualsXPredicate);
    }

    /**
     * Whether the card's target filter contains a {@link PermanentManaValueAtMostXPredicate}, so
     * the announced X must be at least the chosen target's mana value rather than being free to
     * pick.
     */
    public static boolean hasManaValueAtMostXTarget(Card card) {
        TargetFilter filter = card.getTargetFilter();
        return filter instanceof PermanentPredicateTargetFilter pf
                && containsManaValueAtMostXPredicate(pf.predicate());
    }

    private static boolean containsManaValueAtMostXPredicate(PermanentPredicate predicate) {
        if (predicate instanceof PermanentManaValueAtMostXPredicate) {
            return true;
        }
        return predicate instanceof PermanentAllOfPredicate allOf
                && allOf.predicates().stream().anyMatch(AiUtils::containsManaValueAtMostXPredicate);
    }

    /**
     * Minimum number of creatures that must be assigned together for the attacker to become
     * blocked. Multiple restrictions combine by taking the largest minimum.
     */
    static int minimumBlockersRequiredToBlock(GameData gameData, GameQueryService gameQueryService,
                                              Permanent attacker) {
        int minimum = gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE) ? 2 : 1;
        for (var effect : attacker.getCard().getStaticEffects()) {
            if (effect instanceof CantBeBlockedByFewerThanNCreaturesEffect restriction) {
                minimum = Math.max(minimum, restriction.minBlockers());
            }
        }
        return minimum;
    }

    static UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Whether an AI seated as {@code aiPlayerId} must answer a choice addressed to
     * {@code choicePlayerId}. Normally only its own choices — but during a
     * Mindslaver-controlled turn the engine routes the controlled player's prompts
     * to the controlling player's connection and substitutes the acting player when
     * the answer arrives ({@code GameService.resolveActingPlayer}), so the
     * controller must answer those too.
     */
    static boolean isRespondingFor(GameData gameData, UUID aiPlayerId, UUID choicePlayerId) {
        if (aiPlayerId.equals(choicePlayerId)) {
            return true;
        }
        return aiPlayerId.equals(gameData.mindControllerPlayerId())
                && choicePlayerId != null
                && choicePlayerId.equals(gameData.mindControlledPlayerId());
    }
}
