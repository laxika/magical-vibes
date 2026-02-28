package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapSubtypeBoostSelfAndDamageDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetUntapWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.UntapEachOtherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatureModResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @HandlesEffect(AnimateLandEffect.class)
    private void resolveAnimateLand(GameData gameData, StackEntry entry, AnimateLandEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(effect.power());
        self.setAnimatedToughness(effect.toughness());
        self.setAnimatedColor(effect.animatedColor());
        self.getGrantedSubtypes().clear();
        self.getGrantedSubtypes().addAll(effect.grantedSubtypes());
        self.getGrantedKeywords().addAll(effect.grantedKeywords());

        String logEntry = self.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness() + " creature with flying until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), effect.power(), effect.toughness());
    }

    @HandlesEffect(AnimateSelfEffect.class)
    private void resolveAnimateSelf(GameData gameData, StackEntry entry, AnimateSelfEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        int xValue = entry.getXValue();
        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(xValue);
        self.setAnimatedToughness(xValue);
        self.getGrantedSubtypes().clear();
        self.getGrantedSubtypes().addAll(effect.grantedSubtypes());

        String logEntry = self.getCard().getName() + " becomes a " + xValue + "/" + xValue + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), xValue, xValue);
    }

    @HandlesEffect(AnimateSelfByChargeCountersEffect.class)
    private void resolveAnimateSelfByChargeCounters(GameData gameData, StackEntry entry, AnimateSelfByChargeCountersEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        int counters = self.getChargeCounters();
        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(counters);
        self.setAnimatedToughness(counters);
        self.getGrantedSubtypes().clear();
        self.getGrantedSubtypes().addAll(effect.grantedSubtypes());

        String logEntry = self.getCard().getName() + " becomes a " + counters + "/" + counters + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), counters, counters);
    }

    @HandlesEffect(AnimateSelfWithStatsEffect.class)
    private void resolveAnimateSelfWithStats(GameData gameData, StackEntry entry, AnimateSelfWithStatsEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(effect.power());
        self.setAnimatedToughness(effect.toughness());
        self.getGrantedSubtypes().clear();
        self.getGrantedSubtypes().addAll(effect.grantedSubtypes());
        self.getGrantedKeywords().addAll(effect.grantedKeywords());

        String logEntry = self.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness()
                + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature with {}",
                gameData.id, self.getCard().getName(), effect.power(), effect.toughness(), effect.grantedKeywords());
    }

    @HandlesEffect(AddCardTypeToTargetPermanentEffect.class)
    private void resolveAddCardTypeToTargetPermanent(GameData gameData, StackEntry entry, AddCardTypeToTargetPermanentEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getGrantedCardTypes().add(effect.cardType());

        String typeName = effect.cardType().name().charAt(0) + effect.cardType().name().substring(1).toLowerCase();
        String logEntry = target.getCard().getName() + " becomes an " + typeName + " in addition to its other types until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes an {} until end of turn", gameData.id, target.getCard().getName(), typeName);
    }

    @HandlesEffect(BoostSelfEffect.class)
    private void resolveBoostSelf(GameData gameData, StackEntry entry, BoostSelfEffect boost) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        self.setPowerModifier(self.getPowerModifier() + boost.powerBoost());
        self.setToughnessModifier(self.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = self.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, self.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    @HandlesEffect(BoostSelfPerBlockingCreatureEffect.class)
    private void resolveBoostSelfPerBlockingCreature(GameData gameData, StackEntry entry, BoostSelfPerBlockingCreatureEffect boost) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        List<Permanent> selfBattlefield = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(self)) {
                selfBattlefield = battlefield;
                break;
            }
        }
        if (selfBattlefield == null) return;

        int selfIndex = selfBattlefield.indexOf(self);
        if (selfIndex < 0) {
            return;
        }

        final int[] blockerCount = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargets().contains(selfIndex)) {
                blockerCount[0]++;
            }
        });

        int powerBoost = blockerCount[0] * boost.powerPerBlockingCreature();
        int toughnessBoost = blockerCount[0] * boost.toughnessPerBlockingCreature();
        self.setPowerModifier(self.getPowerModifier() + powerBoost);
        self.setToughnessModifier(self.getToughnessModifier() + toughnessBoost);

        String logEntry = self.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + blockerCount[0] + " blocker(s)).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} blocker(s)",
                gameData.id, self.getCard().getName(), powerBoost, toughnessBoost, blockerCount[0]);
    }

    @HandlesEffect(BoostTargetCreatureEffect.class)
    private void resolveBoostTargetCreature(GameData gameData, StackEntry entry, BoostTargetCreatureEffect boost) {
        // Multi-target: apply boost to each valid target
        if (entry.getTargetPermanentIds() != null && !entry.getTargetPermanentIds().isEmpty()) {
            for (UUID targetId : entry.getTargetPermanentIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
                target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

                String logEntry = target.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
        target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = target.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    @HandlesEffect(BoostFirstTargetCreatureEffect.class)
    private void resolveBoostFirstTargetCreature(GameData gameData, StackEntry entry, BoostFirstTargetCreatureEffect boost) {
        if (entry.getTargetPermanentIds() == null || entry.getTargetPermanentIds().isEmpty()) {
            return;
        }

        UUID firstTargetId = entry.getTargetPermanentIds().getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, firstTargetId);
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
        target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = target.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    @HandlesEffect(BoostAllOwnCreaturesEffect.class)
    private void resolveBoostAllOwnCreatures(GameData gameData, StackEntry entry, BoostAllOwnCreaturesEffect boost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (boost.filter() == null
                        || gameQueryService.matchesPermanentPredicate(permanent, boost.filter(), filterContext))) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{}", gameData.id, entry.getCard().getName(), count, boost.powerBoost(), boost.toughnessBoost());
    }

    @HandlesEffect(BoostAllCreaturesXEffect.class)
    private void resolveBoostAllCreaturesX(GameData gameData, StackEntry entry, BoostAllCreaturesXEffect effect) {
        int xValue = entry.getXValue();
        int powerBoost = effect.powerMultiplier() * xValue;
        int toughnessBoost = effect.toughnessMultiplier() * xValue;

        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (effect.filter() == null
                        || gameQueryService.matchesPermanentPredicate(gameData, permanent, effect.filter()))) {
                permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
                count[0]++;
            }
        });

        String logEntry = String.format("%s gives %+d/%+d to %d creature(s) until end of turn.",
                entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gives {}/{} to {} creatures", gameData.id, entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
    }

    @HandlesEffect(GrantKeywordEffect.class)
    private void resolveGrantKeyword(GameData gameData, StackEntry entry, GrantKeywordEffect grant) {
        if (grant.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (grant.filter() != null
                        && !gameQueryService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    continue;
                }
                permanent.getGrantedKeywords().add(grant.keyword());
                count++;
            }

            String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
            String logEntry = entry.getCard().getName() + " gives " + keywordName + " to " + count + " creature(s) until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} own creature(s)", gameData.id, entry.getCard().getName(), grant.keyword(), count);
            return;
        }

        UUID targetId = switch (grant.scope()) {
            case SELF -> entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
            case TARGET -> entry.getTargetPermanentId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        target.getGrantedKeywords().add(grant.keyword());
        String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keyword(), grant.scope());
    }

    @HandlesEffect(GrantChosenKeywordToTargetEffect.class)
    private void resolveGrantChosenKeyword(GameData gameData, StackEntry entry, GrantChosenKeywordToTargetEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        playerInputService.beginKeywordChoice(gameData, entry.getControllerId(), target.getId(), effect.options());
    }

    @HandlesEffect(CantBlockSourceEffect.class)
    private void resolveCantBlockSource(GameData gameData, StackEntry entry, CantBlockSourceEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null || effect.sourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, effect.sourcePermanentId());
        String sourceName = source != null ? source.getCard().getName() : entry.getCard().getName();

        target.getCantBlockIds().add(effect.sourcePermanentId());

        String logEntry = target.getCard().getName() + " can't block " + sourceName + " this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} can't block {} this turn", gameData.id, target.getCard().getName(), sourceName);
    }

    @HandlesEffect(MustBlockSourceEffect.class)
    private void resolveMustBlockSource(GameData gameData, StackEntry entry, MustBlockSourceEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null || effect.sourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, effect.sourcePermanentId());
        String sourceName = source != null ? source.getCard().getName() : entry.getCard().getName();

        target.getMustBlockIds().add(effect.sourcePermanentId());

        String logEntry = target.getCard().getName() + " must block " + sourceName + " this turn if able.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} must block {} this turn if able", gameData.id, target.getCard().getName(), sourceName);
    }

    @HandlesEffect(TargetCreatureCantBlockThisTurnEffect.class)
    private void resolveCantBlockTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setCantBlockThisTurn(true);

        String logEntry = target.getCard().getName() + " can't block this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }

    @HandlesEffect(MakeTargetUnblockableEffect.class)
    private void resolveMakeTargetUnblockable(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setCantBeBlocked(true);

        String logEntry = target.getCard().getName() + " can't be blocked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} can't be blocked this turn", gameData.id, target.getCard().getName());
    }

    @HandlesEffect(TapCreaturesEffect.class)
    private void resolveTapCreatures(GameData gameData, StackEntry entry, TapCreaturesEffect tap) {
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (!gameQueryService.matchesFilters(
                    p,
                    tap.filters(),
                    FilterContext.of(gameData)
                            .withSourceCardId(entry.getCard().getId())
                            .withSourceControllerId(entry.getControllerId()))) return;

            boolean wasTapped = p.isTapped();
            p.tap();
            if (!wasTapped) {
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, p);
            }

            String logMsg = entry.getCard().getName() + " taps " + p.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
        });

        log.info("Game {} - {} taps creatures matching filters", gameData.id, entry.getCard().getName());
    }

    @HandlesEffect(TapOrUntapTargetPermanentEffect.class)
    private void resolveTapOrUntapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (target.isTapped()) {
            target.untap();
            String logEntry = entry.getCard().getName() + " untaps " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
        } else {
            target.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, target);
            String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
        }
    }

    @HandlesEffect(TapTargetPermanentEffect.class)
    private void resolveTapTargetPermanent(GameData gameData, StackEntry entry) {
        // Multi-target: tap each valid target
        if (entry.getTargetPermanentIds() != null && !entry.getTargetPermanentIds().isEmpty()) {
            for (UUID targetId : entry.getTargetPermanentIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                boolean wasTapped = target.isTapped();
                target.tap();
                if (!wasTapped) {
                    triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, target);
                }
                String logMsg = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        boolean wasTapped = target.isTapped();
        target.tap();
        if (!wasTapped) {
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, target);
        }

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    @HandlesEffect(PreventTargetUntapWhileSourceTappedEffect.class)
    private void resolvePreventTargetUntapWhileSourceTapped(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        target.getUntapPreventedByPermanentIds().add(sourcePermanentId);

        String logEntry = target.getCard().getName() + " won't untap as long as " + entry.getCard().getName() + " remains tapped.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untap prevented while {} remains tapped", gameData.id, target.getCard().getName(), entry.getCard().getName());
    }

    @HandlesEffect(UntapTargetPermanentEffect.class)
    private void resolveUntapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.untap();

        String logEntry = entry.getCard().getName() + " untaps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    @HandlesEffect(UntapSelfEffect.class)
    private void resolveUntapSelf(GameData gameData, StackEntry entry) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.untap();

        String logEntry = entry.getCard().getName() + " untaps.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
    }

    @HandlesEffect(UntapAttackedCreaturesEffect.class)
    private void resolveUntapAttackedCreatures(GameData gameData, StackEntry entry) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)) return;
            if (!permanent.isAttackedThisTurn()) return;
            if (!permanent.isTapped()) return;

            permanent.untap();
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " untaps " + count[0] + " creature(s) that attacked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} attacked creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    @HandlesEffect(UntapEachOtherCreatureYouControlEffect.class)
    private void resolveUntapEachOtherCreatureYouControl(GameData gameData, StackEntry entry, UntapEachOtherCreatureYouControlEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourceId = entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (p.getId().equals(sourceId)) continue;
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (effect.filter() != null
                    && !gameQueryService.matchesPermanentPredicate(p, effect.filter(), filterContext)) continue;
            if (!p.isTapped()) continue;

            p.untap();
            count++;
        }

        String logEntry = entry.getCard().getName() + " untaps " + count + " other creature(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} other creature(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEachOtherCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEachOtherCreature(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        final int[] count = {0};

        gameData.forEachPermanent((playerId, p) -> {
            if (p.getId().equals(sourceId)) return;
            if (!gameQueryService.isCreature(gameData, p)) return;

            p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + 1);
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count[0] + " other creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} other creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControls(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.isCreature(gameData, p)) continue;

            p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + 1);
            count++;
        }

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count + " creature(s) target player controls.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} creature(s) target player controls", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(PutCountersOnSourceEffect.class)
    private void resolvePutCountersOnSource(GameData gameData, StackEntry entry, PutCountersOnSourceEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        String counterLabel = String.format("%+d/%+d", effect.powerModifier(), effect.toughnessModifier());
        if (effect.powerModifier() > 0) {
            source.setPlusOnePlusOneCounters(source.getPlusOnePlusOneCounters() + effect.amount());
        } else {
            source.setMinusOneMinusOneCounters(source.getMinusOneMinusOneCounters() + effect.amount());
        }
        String logEntry = source.getCard().getName() + " gets " + effect.amount() + " " + counterLabel + " counter(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} {} counter(s)", gameData.id, source.getCard().getName(), effect.amount(), counterLabel);
    }

    @HandlesEffect(PutChargeCounterOnSelfEffect.class)
    private void resolvePutChargeCounterOnSelf(GameData gameData, StackEntry entry) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setChargeCounters(self.getChargeCounters() + 1);

        String logEntry = self.getCard().getName() + " gets a charge counter (" + self.getChargeCounters() + " total).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a charge counter ({} total)", gameData.id, self.getCard().getName(), self.getChargeCounters());
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnTargetCreature(GameData gameData, StackEntry entry, PutMinusOneMinusOneCounterOnTargetCreatureEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        int count = effect.count();
        target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + count);

        String counterText = count == 1 ? "a -1/-1 counter" : count + " -1/-1 counters";
        String logEntry = target.getCard().getName() + " gets " + counterText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} -1/-1 counter(s)", gameData.id, target.getCard().getName(), count);
    }

    @HandlesEffect(GrantColorUntilEndOfTurnEffect.class)
    private void resolveGrantColorUntilEndOfTurn(GameData gameData, StackEntry entry, GrantColorUntilEndOfTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getGrantedColors().clear();
        target.getGrantedColors().add(effect.color());
        target.setColorOverridden(true);

        String colorName = effect.color().name().charAt(0) + effect.color().name().substring(1).toLowerCase();
        String logEntry = target.getCard().getName() + " becomes " + colorName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
    }

    @HandlesEffect(GrantProtectionFromCardTypeUntilEndOfTurnEffect.class)
    private void resolveGrantProtectionFromCardTypeUntilEndOfTurn(GameData gameData, StackEntry entry, GrantProtectionFromCardTypeUntilEndOfTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getProtectionFromCardTypes().add(effect.cardType());

        String typeName = effect.cardType().getDisplayName().toLowerCase() + "s";
        String logEntry = target.getCard().getName() + " gains protection from " + typeName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), typeName);
    }

    @HandlesEffect(UnattachEquipmentFromTargetPermanentsEffect.class)
    private void resolveUnattachEquipmentFromTargetPermanents(GameData gameData, StackEntry entry) {
        if (entry.getTargetPermanentIds() == null || entry.getTargetPermanentIds().isEmpty()) {
            return;
        }

        // Track creatures that need to be sacrificed due to SacrificeOnUnattachEffect
        Set<UUID> sacrificeTargetIds = new java.util.LinkedHashSet<>();

        for (UUID targetId : entry.getTargetPermanentIds()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            gameData.forEachPermanent((playerId, p) -> {
                if (targetId.equals(p.getAttachedTo())
                        && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    p.setAttachedTo(null);
                    String unattachLog = entry.getCard().getName() + " unattaches " + p.getCard().getName()
                            + " from " + target.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, unattachLog);
                    log.info("Game {} - {} unattaches {} from {}", gameData.id, entry.getCard().getName(),
                            p.getCard().getName(), target.getCard().getName());

                    boolean hasSacrificeOnUnattach = p.getCard().getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);
                    if (hasSacrificeOnUnattach) {
                        sacrificeTargetIds.add(targetId);
                    }
                }
            });
        }

        // Sacrifice creatures that were unattached from equipment with SacrificeOnUnattachEffect
        for (UUID creatureId : sacrificeTargetIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null) {
                String sacrificeLog = creature.getCard().getName() + " is sacrificed (equipment with sacrifice-on-unattach became unattached).";
                gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
                log.info("Game {} - {} sacrificed due to equipment unattach", gameData.id, creature.getCard().getName());
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }

    @HandlesEffect(TapSubtypeBoostSelfAndDamageDefenderEffect.class)
    private void resolveTapSubtypeBoostSelfAndDamageDefender(GameData gameData, StackEntry entry, TapSubtypeBoostSelfAndDamageDefenderEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        // Find all untapped creatures with the required subtype the controller controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (!perm.isTapped()
                        && gameQueryService.isCreature(gameData, perm)
                        && gameQueryService.matchesPermanentPredicate(gameData, perm,
                                new com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate(effect.subtype()))) {
                    eligibleIds.add(perm.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            String logEntry = entry.getCard().getName() + "'s attack ability finds no untapped " + effect.subtype().getDisplayName() + " to tap.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} attack trigger: no eligible {} to tap", gameData.id, entry.getCard().getName(), effect.subtype().getDisplayName());
            return;
        }

        gameData.pendingTapSubtypeBoostSourcePermanentId = sourcePermanentId;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                "You may tap any number of untapped " + effect.subtype().getDisplayName() + " you control.");
    }

    @HandlesEffect(ProliferateEffect.class)
    private void resolveProliferate(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Collect all permanents with counters (any player's battlefield)
        List<UUID> eligiblePermanentIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getPlusOnePlusOneCounters() > 0
                    || p.getMinusOneMinusOneCounters() > 0
                    || p.getLoyaltyCounters() > 0) {
                eligiblePermanentIds.add(p.getId());
            }
        });

        if (eligiblePermanentIds.isEmpty()) {
            String logEntry = "Proliferate: no permanents with counters to choose.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Proliferate: no eligible permanents", gameData.id);
            return;
        }

        // Count total proliferate effects in this stack entry (e.g. "proliferate, then proliferate again")
        // so the handler knows how many rounds of choices remain after this one.
        long totalProliferates = entry.getEffectsToResolve().stream()
                .filter(e -> e instanceof ProliferateEffect)
                .count();
        gameData.pendingProliferateCount = (int) totalProliferates;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligiblePermanentIds,
                eligiblePermanentIds.size(), "Proliferate: Choose permanents to add counters to.");
    }
}

