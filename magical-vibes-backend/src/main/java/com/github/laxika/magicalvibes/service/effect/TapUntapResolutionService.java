package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.PreventTargetUntapWhileSourceOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetUntapWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapAllAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapPermanentsOfTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapAllAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsOfTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.UntapUpToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapEachOtherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TapUntapResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

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

    @HandlesEffect(TapAllAttackingCreaturesEffect.class)
    private void resolveTapAllAttackingCreatures(GameData gameData, StackEntry entry) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!p.isAttacking()) return;
            if (!gameQueryService.isCreature(gameData, p)) return;

            boolean wasTapped = p.isTapped();
            p.tap();
            if (!wasTapped) {
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, p);
                count[0]++;
            }
        });

        String logMsg = entry.getCard().getName() + " taps " + count[0] + " attacking creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} taps {} attacking creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    @HandlesEffect(SkipNextUntapAllAttackingCreaturesEffect.class)
    private void resolveSkipNextUntapAllAttackingCreatures(GameData gameData, StackEntry entry) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!p.isAttacking()) return;
            if (!gameQueryService.isCreature(gameData, p)) return;

            p.setSkipUntapCount(p.getSkipUntapCount() + 1);
            count[0]++;
        });

        String logMsg = entry.getCard().getName() + " prevents " + count[0] + " attacking creature(s) from untapping during their controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} skip next untap set on {} attacking creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    @HandlesEffect(TapSelfEffect.class)
    private void resolveTapSelf(GameData gameData, StackEntry entry) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        boolean wasTapped = self.isTapped();
        self.tap();
        if (!wasTapped) {
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, self);
        }

        String logEntry = self.getCard().getName() + " taps itself.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} taps itself", gameData.id, self.getCard().getName());
    }

    @HandlesEffect(TapEnchantedCreatureEffect.class)
    private void resolveTapEnchantedCreature(GameData gameData, StackEntry entry) {
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (auraPerm == null) {
            log.info("Game {} - Aura {} no longer on battlefield, skipping tap enchanted creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        UUID enchantedId = auraPerm.getAttachedTo();
        if (enchantedId == null) {
            log.info("Game {} - {} is not attached to anything, skipping tap enchanted creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping tap",
                    gameData.id);
            return;
        }

        boolean wasTapped = enchantedCreature.isTapped();
        enchantedCreature.tap();
        if (!wasTapped) {
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, enchantedCreature);
        }

        String logMsg = entry.getCard().getName() + " taps " + enchantedCreature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} taps enchanted creature {}", gameData.id, entry.getCard().getName(), enchantedCreature.getCard().getName());
    }

    @HandlesEffect(TapOrUntapTargetPermanentEffect.class)
    private void resolveTapOrUntapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
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
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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

    @HandlesEffect(SkipNextUntapOnTargetEffect.class)
    private void resolveSkipNextUntapOnTarget(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setSkipUntapCount(target.getSkipUntapCount() + 1);

        String logEntry = target.getCard().getName() + " won't untap during its controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} skip next untap set", gameData.id, target.getCard().getName());
    }

    @HandlesEffect(PreventTargetUntapWhileSourceTappedEffect.class)
    private void resolvePreventTargetUntapWhileSourceTapped(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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

    @HandlesEffect(PreventTargetUntapWhileSourceOnBattlefieldEffect.class)
    private void resolvePreventTargetUntapWhileSourceOnBattlefield(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        target.getUntapPreventedWhileSourceOnBattlefieldIds().add(sourcePermanentId);

        String logEntry = target.getCard().getName() + " won't untap as long as you control " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untap prevented while {} on battlefield", gameData.id, target.getCard().getName(), entry.getCard().getName());
    }

    @HandlesEffect(UntapAllTargetPermanentsEffect.class)
    private void resolveUntapAllTargetPermanents(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            target.untap();

            String logEntry = entry.getCard().getName() + " untaps " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
        }
    }

    @HandlesEffect(UntapTargetPermanentEffect.class)
    private void resolveUntapTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.untap();

        String logEntry = entry.getCard().getName() + " untaps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    @HandlesEffect(RemoveTargetFromCombatEffect.class)
    private void resolveRemoveTargetFromCombat(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (target.isAttacking()) {
            target.setAttacking(false);
            target.setAttackTarget(null);
        }
        if (target.isBlocking()) {
            target.setBlocking(false);
            target.getBlockingTargets().clear();
            target.getBlockingTargetIds().clear();
        }

        String logEntry = entry.getCard().getName() + " removes " + target.getCard().getName() + " from combat.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} removes {} from combat", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    @HandlesEffect(UntapSelfEffect.class)
    private void resolveUntapSelf(GameData gameData, StackEntry entry) {
        UUID selfId = entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
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

    @HandlesEffect(TapPermanentsOfTargetPlayerEffect.class)
    private void resolveTapPermanentsOfTargetPlayer(GameData gameData, StackEntry entry, TapPermanentsOfTargetPlayerEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.matchesPermanentPredicate(p, effect.filter(), filterContext)) continue;

            boolean wasTapped = p.isTapped();
            p.tap();
            if (!wasTapped) {
                triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, p);
                count++;
            }
        }

        String logMsg = entry.getCard().getName() + " taps " + count + " permanent(s).";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} taps {} permanent(s) of target player", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(SkipNextUntapPermanentsOfTargetPlayerEffect.class)
    private void resolveSkipNextUntapPermanentsOfTargetPlayer(GameData gameData, StackEntry entry, SkipNextUntapPermanentsOfTargetPlayerEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.matchesPermanentPredicate(p, effect.filter(), filterContext)) continue;

            p.setSkipUntapCount(p.getSkipUntapCount() + 1);
            count++;
        }

        String logMsg = entry.getCard().getName() + " prevents " + count + " permanent(s) from untapping during their controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} skip next untap set on {} permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(UntapAllControlledPermanentsEffect.class)
    private void resolveUntapAllControlledPermanents(GameData gameData, StackEntry entry, UntapAllControlledPermanentsEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent p : battlefield) {
            if (effect.filter() != null
                    && !gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter())) continue;
            if (!p.isTapped()) continue;

            p.untap();
            count++;
        }

        String logEntry = entry.getCard().getName() + " untaps " + count + " permanent(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} controlled permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(RegisterDelayedUntapPermanentsEffect.class)
    private void resolveRegisterDelayedUntapPermanents(GameData gameData, StackEntry entry, RegisterDelayedUntapPermanentsEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.pendingDelayedUntapPermanents.add(
                new GameData.DelayedUntapPermanents(controllerId, effect.count(), effect.filter(), entry.getCard()));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed untap up to {} permanents at next end step",
                gameData.id, playerName, effect.count());
    }

    @HandlesEffect(UntapUpToControlledPermanentsEffect.class)
    private void resolveUntapUpToControlledPermanents(GameData gameData, StackEntry entry, UntapUpToControlledPermanentsEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        int remaining = effect.count();
        int count = 0;
        for (Permanent p : battlefield) {
            if (remaining <= 0) break;
            if (!p.isTapped()) continue;
            if (effect.filter() != null
                    && !gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter())) continue;

            p.untap();
            count++;
            remaining--;
        }

        if (count > 0) {
            String logEntry = entry.getCard().getName() + " untaps " + count + " permanent(s).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }
        log.info("Game {} - {} untaps {} permanent(s) via delayed trigger", gameData.id, entry.getCard().getName(), count);
    }
}
