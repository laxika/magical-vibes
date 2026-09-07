package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DayNightService {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final AnimationSupport animationSupport;

    public DayNightService(GameLogService gameLogService,
                           TriggerCollectionService triggerCollectionService,
                           AnimationSupport animationSupport) {
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.animationSupport = animationSupport;
    }

    public void becomeDay(GameData gameData) {
        if (gameData.dayNight == DayNight.DAY) return;
        changeDesignation(gameData, DayNight.DAY);
    }

    public void becomeNight(GameData gameData) {
        if (gameData.dayNight == DayNight.NIGHT) return;
        changeDesignation(gameData, DayNight.NIGHT);
    }

    public void toggle(GameData gameData) {
        if (gameData.dayNight == DayNight.NIGHT) {
            becomeDay(gameData);
        } else {
            becomeNight(gameData);
        }
    }

    public void checkAtUntap(GameData gameData, UUID activePlayerId) {
        DayNight next = switch (gameData.dayNight) {
            case NEITHER -> DayNight.NEITHER;
            case DAY -> gameData.spellsCastLastTurn.getOrDefault(activePlayerId, 0) == 0
                    ? DayNight.NIGHT : DayNight.DAY;
            case NIGHT -> gameData.spellsCastLastTurn.getOrDefault(activePlayerId, 0) >= 2
                    ? DayNight.DAY : DayNight.NIGHT;
        };
        if (next != gameData.dayNight) {
            changeDesignation(gameData, next);
        }
    }

    public void applyDayboundEntryRules(GameData gameData, Permanent enteringPermanent) {
        if (!enteringPermanent.getCard().getKeywords().contains(Keyword.DAYBOUND)) {
            return;
        }

        if (gameData.dayNight == DayNight.NEITHER) {
            becomeDay(gameData);
        } else if (gameData.dayNight == DayNight.NIGHT
                && enteringPermanent.getOriginalCard().getBackFaceCard() != null
                && !enteringPermanent.isTransformed()) {
            enteringPermanent.setCard(enteringPermanent.getOriginalCard().getBackFaceCard());
            enteringPermanent.setTransformed(true);
            enteringPermanent.setAttachedTo(null);
        }
    }

    private void changeDesignation(GameData gameData, DayNight next) {
        DayNight previous = gameData.dayNight;
        gameData.dayNight = next;
        gameLogService.append(gameData, GameLog.text("It becomes " + next.name().toLowerCase() + "."));
        transformDayNightPermanents(gameData, next);
        if (previous != DayNight.NEITHER && previous != next) {
            triggerCollectionService.checkDayNightChangeTriggers(gameData, previous, next);
        } else {
            triggerCollectionService.processNextDayNightTriggerTarget(gameData);
        }
    }

    private void transformDayNightPermanents(GameData gameData, DayNight designation) {
        List<Permanent> toBack = new ArrayList<>();
        List<Permanent> toFront = new ArrayList<>();
        List<UUID> toBackControllers = new ArrayList<>();
        List<UUID> toFrontControllers = new ArrayList<>();

        gameData.forEachPermanent((controllerId, permanent) -> {
            if (designation == DayNight.NIGHT
                && permanent.getCard().getKeywords().contains(Keyword.DAYBOUND)) {
                toBack.add(permanent);
                toBackControllers.add(controllerId);
            } else if (designation == DayNight.DAY
                    && permanent.getCard().getKeywords().contains(Keyword.NIGHTBOUND)) {
                toFront.add(permanent);
                toFrontControllers.add(controllerId);
            }
        });

        for (int i = 0; i < toBack.size(); i++) {
            animationSupport.transformToBackFaceForDayNight(gameData, toBack.get(i));
        }
        for (int i = 0; i < toFront.size(); i++) {
            Permanent permanent = toFront.get(i);
            if (animationSupport.transformToFrontFaceForDayNight(gameData, permanent)
                    && permanent.getCard().isAura()
                    && permanent.getCard().isEnchantPlayer()) {
                gameData.queueInteraction(new PermanentChoiceContext.DayNightTransformAttachment(
                        permanent.getId(), toFrontControllers.get(i)));
            }
        }
    }
}
