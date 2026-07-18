package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PackHuntEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSubtype;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PackHuntEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PackHuntEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PackHuntEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        UUID controllerId = entry.getControllerId();
        CardSubtype huntSubtype = e.creatureSubtype();

        // Step 1: Tap all untapped creatures of the given subtype the controller controls
        List<Permanent> tappedHunters = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (!p.isTapped()
                        && gameQueryService.isCreature(gameData, p)
                        && p.getCard().getSubtypes().contains(huntSubtype)) {
                    p.tap();
                    tappedHunters.add(p);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", p.getCard(), "."));
                }
            }
        }

        if (tappedHunters.isEmpty()) return;

        // Step 2: Each creature tapped this way deals damage equal to its power to target creature
        for (Permanent hunter : tappedHunters) {
            int hunterPower = gameQueryService.getPowerBasedDamage(gameData, hunter);
            if (!(gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, hunter.getCard()))) {
                int damage = gameQueryService.applyDamageMultiplier(gameData, hunterPower, entry);
                damageSupport.dealCreatureDamage(gameData, entry, target, damage, hunter);
            } else {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(hunter.getCard(), "'s damage to ", target.getCard(), " is prevented."));
            }
        }

        // Step 3: Target creature deals damage equal to its power divided evenly among tapped creatures.
        // Skip entirely when targetPower is 0 — the division below would produce no damage and spam 0-damage log lines.
        int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);
        if (targetPower > 0) {
            int baseDamage = targetPower / tappedHunters.size();
            int remainder = targetPower % tappedHunters.size();

            for (int i = 0; i < tappedHunters.size(); i++) {
                Permanent hunter = tappedHunters.get(i);
                int damage = baseDamage + (i < remainder ? 1 : 0);
                if (damage > 0) {
                    if (!(gameQueryService.isDamagePreventable(gameData)
                            && gameQueryService.hasProtectionFromSource(gameData, hunter, target.getCard()))) {
                        int actualDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
                        damageSupport.dealCreatureDamage(gameData, entry, hunter, actualDamage, target);
                    } else {
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(target.getCard(), "'s damage to ", hunter.getCard(), " is prevented."));
                    }
                }
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
