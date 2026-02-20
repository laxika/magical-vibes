package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
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
public class CreatureModResolutionService implements EffectHandlerProvider {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(AnimateSelfEffect.class,
                (gd, entry, effect) -> resolveAnimateSelf(gd, entry, (AnimateSelfEffect) effect));
        registry.register(BoostSelfEffect.class,
                (gd, entry, effect) -> resolveBoostSelf(gd, entry, (BoostSelfEffect) effect));
        registry.register(BoostSelfPerBlockingCreatureEffect.class,
                (gd, entry, effect) -> resolveBoostSelfPerBlockingCreature(gd, entry, (BoostSelfPerBlockingCreatureEffect) effect));
        registry.register(BoostTargetCreatureEffect.class,
                (gd, entry, effect) -> resolveBoostTargetCreature(gd, entry, (BoostTargetCreatureEffect) effect));
        registry.register(BoostTargetBlockingCreatureEffect.class, (gd, entry, effect) -> {
            var boost = (BoostTargetBlockingCreatureEffect) effect;
            resolveBoostTargetCreature(gd, entry, new BoostTargetCreatureEffect(boost.powerBoost(), boost.toughnessBoost()));
        });
        registry.register(BoostAllOwnCreaturesEffect.class,
                (gd, entry, effect) -> resolveBoostAllOwnCreatures(gd, entry, (BoostAllOwnCreaturesEffect) effect));
        registry.register(BoostAllCreaturesXEffect.class,
                (gd, entry, effect) -> resolveBoostAllCreaturesX(gd, entry, (BoostAllCreaturesXEffect) effect));
        registry.register(GrantKeywordToSelfEffect.class,
                (gd, entry, effect) -> resolveGrantKeywordToSelf(gd, entry, (GrantKeywordToSelfEffect) effect));
        registry.register(GrantKeywordToTargetEffect.class,
                (gd, entry, effect) -> resolveGrantKeywordToTarget(gd, entry, (GrantKeywordToTargetEffect) effect));
        registry.register(CantBlockSourceEffect.class,
                (gd, entry, effect) -> resolveCantBlockSource(gd, entry, (CantBlockSourceEffect) effect));
        registry.register(TargetCreatureCantBlockThisTurnEffect.class,
                (gd, entry, effect) -> resolveCantBlockTargetCreature(gd, entry));
        registry.register(MakeTargetUnblockableEffect.class,
                (gd, entry, effect) -> resolveMakeTargetUnblockable(gd, entry));
        registry.register(TapCreaturesEffect.class,
                (gd, entry, effect) -> resolveTapCreatures(gd, entry, (TapCreaturesEffect) effect));
        registry.register(TapOrUntapTargetPermanentEffect.class,
                (gd, entry, effect) -> resolveTapOrUntapTargetPermanent(gd, entry));
        registry.register(TapTargetCreatureEffect.class,
                (gd, entry, effect) -> resolveTapTargetPermanent(gd, entry));
        registry.register(TapTargetPermanentEffect.class,
                (gd, entry, effect) -> resolveTapTargetPermanent(gd, entry));
        registry.register(UntapTargetPermanentEffect.class,
                (gd, entry, effect) -> resolveUntapTargetPermanent(gd, entry));
        registry.register(UntapSelfEffect.class,
                (gd, entry, effect) -> resolveUntapSelf(gd, entry));
        registry.register(UntapAttackedCreaturesEffect.class,
                (gd, entry, effect) -> resolveUntapAttackedCreatures(gd, entry));
        registry.register(PutPlusOnePlusOneCounterOnSourceEffect.class,
                (gd, entry, effect) -> resolvePutPlusOnePlusOneCounterOnSource(gd, entry, (PutPlusOnePlusOneCounterOnSourceEffect) effect));
    }

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

    private void resolveBoostSelf(GameData gameData, StackEntry entry, BoostSelfEffect boost) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setPowerModifier(self.getPowerModifier() + boost.powerBoost());
        self.setToughnessModifier(self.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = self.getCard().getName() + " gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{}", gameData.id, self.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

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

    private void resolveBoostAllOwnCreatures(GameData gameData, StackEntry entry, BoostAllOwnCreaturesEffect boost) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{}", gameData.id, entry.getCard().getName(), count, boost.powerBoost(), boost.toughnessBoost());
    }

    private void resolveBoostAllCreaturesX(GameData gameData, StackEntry entry, BoostAllCreaturesXEffect effect) {
        int xValue = entry.getXValue();
        int powerBoost = effect.powerMultiplier() * xValue;
        int toughnessBoost = effect.toughnessMultiplier() * xValue;

        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
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

    private void resolveGrantKeywordToSelf(GameData gameData, StackEntry entry, GrantKeywordToSelfEffect grant) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.getGrantedKeywords().add(grant.keyword());

        String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = self.getCard().getName() + " gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {}", gameData.id, self.getCard().getName(), grant.keyword());
    }

    private void resolveGrantKeywordToTarget(GameData gameData, StackEntry entry, GrantKeywordToTargetEffect grant) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getGrantedKeywords().add(grant.keyword());

        String keywordName = grant.keyword().name().charAt(0) + grant.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {}", gameData.id, target.getCard().getName(), grant.keyword());
    }

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

    private void resolveTapCreatures(GameData gameData, StackEntry entry, TapCreaturesEffect tap) {
        boolean controllerOnly = tap.filters().stream().anyMatch(f -> f instanceof ControllerOnlyTargetFilter);

        List<UUID> playerIds = controllerOnly
                ? List.of(entry.getControllerId())
                : gameData.orderedPlayerIds;

        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent p : battlefield) {
                if (!gameQueryService.isCreature(gameData, p)) continue;
                if (!gameQueryService.matchesFilters(gameData, p, tap.filters())) continue;

                p.tap();

                String logMsg = entry.getCard().getName() + " taps " + p.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
            }
        }

        log.info("Game {} - {} taps creatures matching filters", gameData.id, entry.getCard().getName());
    }

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

    private void resolvePutPlusOnePlusOneCounterOnSource(GameData gameData, StackEntry entry, PutPlusOnePlusOneCounterOnSourceEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        source.setPlusOnePlusOneCounters(source.getPlusOnePlusOneCounters() + effect.amount());
        String logEntry = source.getCard().getName() + " gets a +1/+1 counter.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} +1/+1 counter(s)", gameData.id, source.getCard().getName(), effect.amount());
    }
}

