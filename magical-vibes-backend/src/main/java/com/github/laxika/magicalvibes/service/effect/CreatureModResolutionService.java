package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatureModResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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

        int blockerCount = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.isBlocking() && permanent.getBlockingTargets().contains(selfIndex)) {
                    blockerCount++;
                }
            }
        }

        int powerBoost = blockerCount * boost.powerPerBlockingCreature();
        int toughnessBoost = blockerCount * boost.toughnessPerBlockingCreature();
        self.setPowerModifier(self.getPowerModifier() + powerBoost);
        self.setToughnessModifier(self.getToughnessModifier() + toughnessBoost);

        String logEntry = self.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + blockerCount + " blocker(s)).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} blocker(s)",
                gameData.id, self.getCard().getName(), powerBoost, toughnessBoost, blockerCount);
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
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (boost.filter() == null
                        || gameQueryService.matchesPermanentPredicate(gameData, permanent, boost.filter()))) {
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

        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && (effect.filter() == null
                            || gameQueryService.matchesPermanentPredicate(gameData, permanent, effect.filter()))) {
                    permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
                    permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
                    count++;
                }
            }
        }

        String logEntry = String.format("%s gives %+d/%+d to %d creature(s) until end of turn.",
                entry.getCard().getName(), powerBoost, toughnessBoost, count);
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gives {}/{} to {} creatures", gameData.id, entry.getCard().getName(), powerBoost, toughnessBoost, count);
    }

    @HandlesEffect(GrantKeywordEffect.class)
    private void resolveGrantKeyword(GameData gameData, StackEntry entry, GrantKeywordEffect grant) {
        if (grant.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
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
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent p : battlefield) {
                if (!gameQueryService.isCreature(gameData, p)) continue;
                if (!gameQueryService.matchesFilters(
                        p,
                        tap.filters(),
                        FilterContext.of(gameData)
                                .withSourceCardId(entry.getCard().getId())
                                .withSourceControllerId(entry.getControllerId()))) continue;

                p.tap();

                String logMsg = entry.getCard().getName() + " taps " + p.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
            }
        }

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
            String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
        }
    }

    @HandlesEffect(TapTargetPermanentEffect.class)
    private void resolveTapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.tap();

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
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
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) continue;
                if (!permanent.isAttackedThisTurn()) continue;
                if (!permanent.isTapped()) continue;

                permanent.untap();
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " untaps " + count + " creature(s) that attacked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} attacked creature(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEachOtherCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEachOtherCreature(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        int count = 0;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent p : battlefield) {
                if (p.getId().equals(sourceId)) continue;
                if (!gameQueryService.isCreature(gameData, p)) continue;

                p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + 1);
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count + " other creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} other creature(s)", gameData.id, entry.getCard().getName(), count);
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
}

