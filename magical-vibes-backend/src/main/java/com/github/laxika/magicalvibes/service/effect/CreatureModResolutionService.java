package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
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
        registry.register(BoostTargetCreatureEffect.class,
                (gd, entry, effect) -> resolveBoostTargetCreature(gd, entry, (BoostTargetCreatureEffect) effect));
        registry.register(BoostTargetBlockingCreatureEffect.class, (gd, entry, effect) -> {
            var boost = (BoostTargetBlockingCreatureEffect) effect;
            resolveBoostTargetCreature(gd, entry, new BoostTargetCreatureEffect(boost.powerBoost(), boost.toughnessBoost()));
        });
        registry.register(BoostAllOwnCreaturesEffect.class,
                (gd, entry, effect) -> resolveBoostAllOwnCreatures(gd, entry, (BoostAllOwnCreaturesEffect) effect));
        registry.register(GrantKeywordToTargetEffect.class,
                (gd, entry, effect) -> resolveGrantKeywordToTarget(gd, entry, (GrantKeywordToTargetEffect) effect));
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
        registry.register(UntapSelfEffect.class,
                (gd, entry, effect) -> resolveUntapSelf(gd, entry));
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
}
