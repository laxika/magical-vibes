package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetColorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetTargetColorEffect) effect;
        CardColor color = e.color();
        if (e.chooseColor()) {
            if (gameData.chosenSpellColor == null) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                playerInputService.beginSpellColorChoice(gameData, entry.getControllerId());
                return;
            }
            color = gameData.chosenSpellColor;
            gameData.chosenSpellColor = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        UUID targetId = entry.getTargetId();
        // A null color is "becomes colorless" (Ersatz Gnomes) — the empty replacement set of CR 105.3.
        String colorName = color == null
                ? "colorless"
                : color.name().charAt(0) + color.name().substring(1).toLowerCase();

        // Target may be a permanent or, like Glamerdye, a spell still on the stack.
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            // CR 105.3 / 611.2b: "becomes [color]" with no stated duration is a floating layer-5 color
            // setter that replaces all previous colors and lasts indefinitely (EffectDuration.PERMANENT).
            // The legacy fields are seeded for direct getEffectiveColor callers; the layered pass replays
            // the floating effect at its real timestamp.
            target.getTransientColors().clear();
            if (color != null) {
                target.getTransientColors().add(color);
            }
            target.setColorOverridden(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), e,
                    target.getId(), null, null, EffectDuration.PERMANENT, 0));

            
            gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" becomes " + colorName + ".").build());
            log.info("Game {} - {} becomes {}", gameData.id, target.getCard().getName(), colorName);
            return;
        }

        // A spell target: record the color so it carries onto the permanent that spell resolves into
        // (CR 400.7a). For instants/sorceries it is a no-op. An empty set records "becomes colorless".
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (targetSpell != null) {
            gameData.spellColorOverrides.put(targetId,
                    color == null ? Set.of() : Set.of(color));
            
            gameLogService.append(gameData, GameLog.builder().card(targetSpell.getCard()).text(" becomes " + colorName + ".").build());
            log.info("Game {} - spell {} becomes {}", gameData.id, targetSpell.getCard().getName(), colorName);
        }
    }
}
