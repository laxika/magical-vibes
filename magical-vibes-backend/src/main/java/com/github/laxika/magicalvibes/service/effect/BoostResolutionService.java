package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesByGreatestPowerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostFirstTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSecondTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerBlockingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureXEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TapSubtypeBoostSelfAndDamageDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoostResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

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

    @HandlesEffect(BoostSelfPerControlledPermanentEffect.class)
    private void resolveBoostSelfPerControlledPermanent(GameData gameData, StackEntry entry, BoostSelfPerControlledPermanentEffect boost) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            for (Permanent permanent : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(permanent, boost.filter(), filterContext)) {
                    count++;
                }
            }
        }

        int powerBoost = count * boost.powerPerPermanent();
        int toughnessBoost = count * boost.toughnessPerPermanent();
        self.setPowerModifier(self.getPowerModifier() + powerBoost);
        self.setToughnessModifier(self.getToughnessModifier() + toughnessBoost);

        String logEntry = self.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + count + " matching permanent(s)).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} matching permanent(s)",
                gameData.id, self.getCard().getName(), powerBoost, toughnessBoost, count);
    }

    @HandlesEffect(BoostTargetCreaturePerControlledPermanentEffect.class)
    private void resolveBoostTargetCreaturePerControlledPermanent(GameData gameData, StackEntry entry, BoostTargetCreaturePerControlledPermanentEffect boost) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            for (Permanent permanent : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(permanent, boost.filter(), filterContext)) {
                    count++;
                }
            }
        }

        int powerBoost = count * boost.powerPerPermanent();
        int toughnessBoost = count * boost.toughnessPerPermanent();
        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = target.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + count + " creature(s)).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} creature(s)",
                gameData.id, target.getCard().getName(), powerBoost, toughnessBoost, count);
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

    @HandlesEffect(BoostTargetCreatureXEffect.class)
    private void resolveBoostTargetCreatureX(GameData gameData, StackEntry entry, BoostTargetCreatureXEffect effect) {
        int xValue = entry.getXValue();
        int powerBoost = effect.powerMultiplier() * xValue;
        int toughnessBoost = effect.toughnessMultiplier() * xValue;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = String.format("%s gets %+d/%+d until end of turn.",
                target.getCard().getName(), powerBoost, toughnessBoost);
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
    }

    @HandlesEffect(SwitchPowerToughnessEffect.class)
    private void resolveSwitchPowerToughness(GameData gameData, StackEntry entry, SwitchPowerToughnessEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setPowerToughnessSwitched(!target.isPowerToughnessSwitched());

        String logEntry = target.getCard().getName() + "'s power and toughness are switched until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {}'s power and toughness switched", gameData.id, target.getCard().getName());
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

    @HandlesEffect(BoostSecondTargetCreatureEffect.class)
    private void resolveBoostSecondTargetCreature(GameData gameData, StackEntry entry, BoostSecondTargetCreatureEffect boost) {
        if (entry.getTargetPermanentIds() == null || entry.getTargetPermanentIds().size() < 2) {
            return;
        }

        UUID secondTargetId = entry.getTargetPermanentIds().get(1);
        Permanent target = gameQueryService.findPermanentById(gameData, secondTargetId);
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + boost.powerBoost());
        target.setToughnessModifier(target.getToughnessModifier() + boost.toughnessBoost());

        String logEntry = target.getCard().getName() + " gets " + boost.powerBoost() + "/" + boost.toughnessBoost() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
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

    @HandlesEffect(BoostAllOwnCreaturesByGreatestPowerEffect.class)
    private void resolveBoostAllOwnCreaturesByGreatestPower(GameData gameData, StackEntry entry, BoostAllOwnCreaturesByGreatestPowerEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());

        // Find the greatest power among creatures the controller controls
        int greatestPower = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                int power = gameQueryService.getEffectivePower(gameData, permanent);
                if (power > greatestPower) {
                    greatestPower = power;
                }
            }
        }

        // Apply +X/+X where X is the greatest power
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.setPowerModifier(permanent.getPowerModifier() + greatestPower);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + greatestPower);
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + greatestPower + "/+" + greatestPower + " to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{}", gameData.id, entry.getCard().getName(), count, greatestPower, greatestPower);
    }

    @HandlesEffect(BoostAllCreaturesEffect.class)
    private void resolveBoostAllCreatures(GameData gameData, StackEntry entry, BoostAllCreaturesEffect boost) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (boost.filter() == null
                        || gameQueryService.matchesPermanentPredicate(permanent, boost.filter(), filterContext))) {
                permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
                permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
                count[0]++;
            }
        });

        String logEntry = entry.getCard().getName() + " gives +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + " to " + count[0] + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{}", gameData.id, entry.getCard().getName(), count[0], boost.powerBoost(), boost.toughnessBoost());
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

    @HandlesEffect(SetBasePowerToughnessUntilEndOfTurnEffect.class)
    private void resolveSetBasePowerToughness(GameData gameData, StackEntry entry, SetBasePowerToughnessUntilEndOfTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
        target.setBasePowerOverride(effect.power());
        target.setBaseToughnessOverride(effect.toughness());

        String logEntry = target.getCard().getName() + " has base power and toughness " + effect.power() + "/" + effect.toughness() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} base P/T set to {}/{}", gameData.id, target.getCard().getName(), effect.power(), effect.toughness());
    }

    @HandlesEffect(BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect.class)
    private void resolveBoostAllOwnCreaturesByCreatureCardsInGraveyard(GameData gameData, StackEntry entry, BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        // Count creature cards in controller's graveyard
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int x = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.hasType(CardType.CREATURE)) {
                    x++;
                }
            }
        }

        if (x == 0) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + " gives +0/+0 (no creature cards in graveyard).");
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.setPowerModifier(permanent.getPowerModifier() + x);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + x);
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " gives +" + x + "/+" + x + " to " + count + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} boosts {} creatures +{}/+{} (creature cards in graveyard)", gameData.id, entry.getCard().getName(), count, x, x);
    }
}
