package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandChooseCardFromItAndExileAllCopiesEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealHandChooseCardFromItAndExileAllCopiesEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandChooseCardFromItAndExileAllCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandChooseCardFromItAndExileAllCopiesEffect) effect;

        playerInputService.beginRevealHandChooseCardFromItAndExileAllCopiesChoice(
                gameData, entry.getControllerId(), entry.getTargetId(),
                card -> predicateEvaluationService.matchesCardPredicate(card, e.choosableFilter(), null),
                CardPredicateUtils.describeFilter(e.choosableFilter()), entry.getCard());
    }
}
