package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PackHuntEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;

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
                        && gameQueryService.hasEffectiveSubtype(gameData, p, huntSubtype)) {
                    p.tap();
                    tappedHunters.add(p);
                    gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", p.getCard(), "."));
                }
            }
        }

        if (tappedHunters.isEmpty()) return;

        // Step 2: Each creature tapped this way deals damage equal to its power to target creature
        for (Permanent hunter : tappedHunters) {
            int hunterPower = gameQueryService.getPowerBasedDamage(gameData, hunter);
            if (!(gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, hunter))) {
                int damage = hunterPower;
                damageSupport.dealCreatureDamage(gameData, entry, target, damage, hunter);
            } else {
                gameLogService.append(gameData, GameLog.cardTextCard(hunter.getCard(), "'s damage to ", target.getCard(), " is prevented."));
            }
        }

        // Step 3: Target creature deals damage equal to its power divided as its controller chooses
        // among the creatures tapped this way. Skip entirely when its power is 0.
        int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);
        if (targetPower > 0) {
            if (tappedHunters.size() == 1) {
                Permanent hunter = tappedHunters.getFirst();
                if (!(gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(gameData, hunter, target))) {
                    damageSupport.dealCreatureDamage(gameData, entry, hunter, targetPower, target);
                } else {
                    gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), "'s damage to ", hunter.getCard(), " is prevented."));
                }
            } else {
                UUID choosingPlayerId = gameQueryService.findPermanentController(gameData, target.getId());
                List<UUID> hunterIds = tappedHunters.stream().map(Permanent::getId).toList();
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.PackHuntDamageAssignment(
                        entry.getCard(), entry.getControllerId(), target.getId(), choosingPlayerId, hunterIds,
                        java.util.Map.of(), targetPower));
                playerInputService.beginPermanentChoice(gameData, choosingPlayerId, hunterIds,
                        "Choose a creature to be dealt 1 damage (" + targetPower + " left to assign).");
                return;
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
