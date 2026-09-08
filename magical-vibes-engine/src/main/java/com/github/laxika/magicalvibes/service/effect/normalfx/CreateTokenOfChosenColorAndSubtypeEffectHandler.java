package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOfChosenColorAndSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CreateTokenOfChosenColorAndSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenOfChosenColorAndSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenOfChosenColorAndSubtypeEffect tokenEffect =
                (CreateTokenOfChosenColorAndSubtypeEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getChosenColor() == null) {
            return;
        }

        CardColor chosenColor = source.getChosenColor();
        CardSubtype subtype = tokenEffect.subtypeByColor().get(chosenColor);
        if (subtype == null) {
            subtype = source.getChosenSubtype();
        }
        if (subtype == null) {
            return;
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, tokenEffect.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, tokenEffect.toughness(), context);
        CreateTokenEffect token = new CreateTokenEffect(
                subtype.getDisplayName(), power, toughness, chosenColor,
                List.of(subtype), Set.of(), Set.of());
        permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), token, 1,
                entry.getCard().getSetCode());
    }
}
