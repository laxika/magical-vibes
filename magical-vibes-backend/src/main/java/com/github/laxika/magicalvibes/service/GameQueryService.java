package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.AttackingOrBlockingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AttackingTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;
import com.github.laxika.magicalvibes.model.filter.LandSubtypeTargetFilter;
import com.github.laxika.magicalvibes.model.filter.MaxPowerTargetFilter;
import com.github.laxika.magicalvibes.model.filter.NonArtifactNonColorCreatureTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TappedTargetFilter;
import com.github.laxika.magicalvibes.model.filter.WithoutKeywordTargetFilter;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GameQueryService {

    public static final List<String> TEXT_CHANGE_COLOR_WORDS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    public static final List<String> TEXT_CHANGE_LAND_TYPES = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");

    private final List<StaticEffectHandlerProvider> staticEffectProviders;

    private StaticEffectHandlerRegistry staticEffectRegistry;

    @PostConstruct
    public void init() {
        staticEffectRegistry = new StaticEffectHandlerRegistry();
        staticEffectProviders.forEach(p -> p.registerHandlers(staticEffectRegistry));
    }

    record StaticBonus(int power, int toughness, Set<Keyword> keywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), false, List.of());
    }

    public Permanent findPermanentById(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(permanentId)) {
                    return p;
                }
            }
        }
        return null;
    }

    public Card findCardInGraveyardById(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card c : graveyard) {
                if (c.getId().equals(cardId)) {
                    return c;
                }
            }
        }
        return null;
    }

    public UUID getOpponentId(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    UUID getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(data.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    public boolean isCreature(GameData gameData, Permanent permanent) {
        if (permanent.getCard().getType() == CardType.CREATURE) return true;
        if (permanent.getCard().getAdditionalTypes().contains(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (isArtifact(permanent)) return hasAnimateArtifactEffect(gameData);
        return false;
    }

    public boolean isArtifact(Permanent permanent) {
        return permanent.getCard().getType() == CardType.ARTIFACT
                || permanent.getCard().getAdditionalTypes().contains(CardType.ARTIFACT);
    }

    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return permanent.hasKeyword(keyword) || computeStaticBonus(gameData, permanent).keywords().contains(keyword);
    }

    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return permanent.getEffectivePower() + computeStaticBonus(gameData, permanent).power();
    }

    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return permanent.getEffectiveToughness() + computeStaticBonus(gameData, permanent).toughness();
    }

    /**
     * Returns the amount of combat damage this creature assigns.
     * Normally equal to effective power, but some effects (e.g. Bark of Doran)
     * cause a creature to assign damage equal to its toughness when toughness > power.
     */
    public int getEffectiveCombatDamage(GameData gameData, Permanent creature) {
        int power = getEffectivePower(gameData, creature);
        int toughness = getEffectiveToughness(gameData, creature);

        if (toughness > power && hasAuraWithEffect(gameData, creature, AssignCombatDamageWithToughnessEffect.class)) {
            return toughness;
        }

        return power;
    }

    StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        boolean isNaturalCreature = target.getCard().getType() == CardType.CREATURE
                || target.getCard().getAdditionalTypes().contains(CardType.CREATURE);
        StaticBonusAccumulator accumulator = new StaticBonusAccumulator();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            boolean targetOnSameBattlefield = bf.contains(target);
            for (Permanent source : bf) {
                if (source == target) continue;
                StaticEffectContext context = new StaticEffectContext(source, target, targetOnSameBattlefield, gameData);
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                    if (handler != null) {
                        handler.apply(context, effect, accumulator);
                    }
                }
            }
        }
        // Handle characteristic-defining abilities (self-referencing static effects like */* P/T)
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(effect);
            if (selfHandler != null) {
                StaticEffectContext selfContext = new StaticEffectContext(target, target, true, gameData);
                selfHandler.apply(selfContext, effect, accumulator);
            }
        }

        boolean isSelfAnimated = target.isAnimatedUntilEndOfTurn();
        if (!isNaturalCreature && !accumulator.isAnimatedCreature() && !isSelfAnimated) return StaticBonus.NONE;

        int power = accumulator.getPower();
        int toughness = accumulator.getToughness();
        if (accumulator.isAnimatedCreature() && !isSelfAnimated) {
            int manaValue = target.getCard().getManaValue();
            power += manaValue;
            toughness += manaValue;
        }

        return new StaticBonus(power, toughness, accumulator.getKeywords(), accumulator.isAnimatedCreature() || isSelfAnimated, accumulator.getGrantedActivatedAbilities());
    }

    public boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) return false;
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionFromColorsEffect protection && protection.colors().contains(sourceColor)) {
                return true;
            }
        }
        if (target.getChosenColor() != null && target.getChosenColor() == sourceColor) {
            return true;
        }
        return false;
    }

    boolean playerHasShroud(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent source : battlefield) {
            boolean grantsControllerShroud = source.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof GrantControllerShroudEffect);
            if (grantsControllerShroud) {
                return true;
            }
        }
        return false;
    }

    boolean hasAnimateArtifactEffect(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AnimateNoncreatureArtifactsEffect) return true;
                }
            }
        }
        return false;
    }

    public boolean hasAuraWithEffect(GameData gameData, Permanent creature, Class<? extends CardEffect> effectClass) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effectClass.isInstance(effect)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isEnchanted(GameData gameData, Permanent creature) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().isAura()) {
                    return true;
                }
            }
        }
        return false;
    }

    Permanent findEnchantedCreatureByAuraEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            if (p.getAttachedTo() != null) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effectClass.isInstance(effect)) {
                        return findPermanentById(gameData, p.getAttachedTo());
                    }
                }
            }
        }
        return null;
    }

    public boolean matchesFilters(GameData gameData, Permanent permanent, Set<TargetFilter> filters) {
        for (TargetFilter filter : filters) {
            if (filter instanceof WithoutKeywordTargetFilter f) {
                if (hasKeyword(gameData, permanent, f.keyword())) {
                    return false;
                }
            }
        }
        return true;
    }

    public void validateTargetFilter(TargetFilter filter, Permanent target) {
        if (filter instanceof MaxPowerTargetFilter f) {
            if (target.getEffectivePower() > f.maxPower()) {
                throw new IllegalStateException("Target creature's power must be " + f.maxPower() + " or less");
            }
        } else if (filter instanceof AttackingOrBlockingTargetFilter) {
            if (!target.isAttacking() && !target.isBlocking()) {
                throw new IllegalStateException("Target must be an attacking or blocking creature");
            }
        } else if (filter instanceof AttackingTargetFilter) {
            if (!target.isAttacking()) {
                throw new IllegalStateException("Target must be an attacking creature");
            }
        } else if (filter instanceof CreatureColorTargetFilter f) {
            if (!f.colors().contains(target.getCard().getColor())) {
                String colorNames = f.colors().stream()
                        .map(c -> c.name().toLowerCase())
                        .sorted()
                        .reduce((a, b) -> a + " or " + b)
                        .orElse("");
                throw new IllegalStateException("Target must be a " + colorNames + " creature");
            }
        } else if (filter instanceof TappedTargetFilter) {
            if (!target.isTapped()) {
                throw new IllegalStateException("Target must be a tapped creature");
            }
        } else if (filter instanceof NonArtifactNonColorCreatureTargetFilter f) {
            if (isArtifact(target)) {
                throw new IllegalStateException("Target must be a nonartifact creature");
            }
            if (f.excludedColors().contains(target.getCard().getColor())) {
                String colorNames = f.excludedColors().stream()
                        .map(c -> c.name().toLowerCase())
                        .sorted()
                        .reduce((a, b) -> a + " or " + b)
                        .orElse("");
                throw new IllegalStateException("Target must be a non" + colorNames + " creature");
            }
        } else if (filter instanceof LandSubtypeTargetFilter f) {
            if (target.getCard().getType() != CardType.LAND
                    || target.getCard().getSubtypes().stream().noneMatch(f.subtypes()::contains)) {
                String subtypeNames = f.subtypes().stream()
                        .map(CardSubtype::getDisplayName)
                        .sorted()
                        .reduce((a, b) -> a + " or " + b)
                        .orElse("");
                throw new IllegalStateException("Target must be a " + subtypeNames);
            }
        }
    }

    /**
     * Returns the global damage multiplier based on DoubleDamageEffect permanents on the battlefield.
     * Each instance doubles the multiplier (e.g. two Furnaces = 4x damage).
     */
    int getDamageMultiplier(GameData gameData) {
        int multiplier = 1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof DoubleDamageEffect) {
                        multiplier *= 2;
                    }
                }
            }
        }
        return multiplier;
    }

    int applyDamageMultiplier(GameData gameData, int damage) {
        return damage * getDamageMultiplier(gameData);
    }

    boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature) {
        return hasAuraWithEffect(gameData, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)
                || isDamageFromSourcePrevented(gameData, creature.getCard().getColor());
    }

    boolean isDamageFromSourcePrevented(GameData gameData, CardColor sourceColor) {
        return sourceColor != null && gameData.preventDamageFromColors.contains(sourceColor);
    }
}
