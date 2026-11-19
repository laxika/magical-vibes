package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TurnSourceFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnSourceFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = subjectResolutionService.permanent(
                gameData, entry, effect, Subjects.of(((TurnSourceFaceUpEffect) effect).reference()));
        if (source == null || !source.isFaceDown()) {
            return;
        }
        // A manifested permanent represented by an instant or sorcery card stays face down.
        if (source.isManifested()
                && (source.getCard().hasType(CardType.INSTANT) || source.getCard().hasType(CardType.SORCERY))) {
            return;
        }
        gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, source);
    }
}
