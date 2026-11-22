package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveKeywordEffect) effect;

        // Mass one-shot removal floats a per-permanent layer-6 removal. Invert the Skies =
        // opponents; Hour of Devastation = all creatures; Spectacular Pileup = all permanents
        // narrowed to creatures and Vehicles. A step result names what an earlier step acted on (Burn from Within's
        // "if a creature is dealt damage this way, it loses indestructible").
        PermanentSubject named = remove.subject();
        PermanentSubject subject = named.isSet() ? Subjects.narrowed(named, remove.filter())
                : named instanceof PermanentSubject.Source || named instanceof PermanentSubject.Target
                || named instanceof PermanentSubject.ThisWay ? named : null;
        if (subject == null) {
            return;
        }
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
            removeFrom(gameData, entry, remove, permanent);
        }
    }

    private void removeFrom(GameData gameData, StackEntry entry, RemoveKeywordEffect remove, Permanent target) {
        // A one-shot keyword removal is a floating layer-6 effect with its own timestamp: a
        // later-timestamp grant of the same keyword re-adds it (CR 613.7).
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                FaceDownVisibility.sourceName(gameData, entry), null, entry.getControllerId(), remove,
                target.getId(), null, null, remove.duration(), 0));
        String keywordName = remove.keyword().name().charAt(0) + remove.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String durationLabel = Duration.UNTIL_END_OF_TURN.equals(remove.duration()) ? " until end of turn" : "";
        
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" loses " + keywordName + durationLabel + ".").build());
        log.info("Game {} - {} loses {} ({})", gameData.id, target.getCard().getName(), remove.keyword(),
                remove.subject());
    }
}
