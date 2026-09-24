package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DraftFromSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftFromSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DraftFromSpellbookEffect draft = (DraftFromSpellbookEffect) effect;
        List<java.util.function.Supplier<? extends Card>> shuffled = new ArrayList<>(draft.cardFactories());
        Collections.shuffle(shuffled);

        List<ChooseOneEffect.ChooseOneOption> options = shuffled.stream()
                .limit(draft.offeredCardCount())
                .map(factory -> {
                    Card preview = factory.get();
                    return new ChooseOneEffect.ChooseOneOption(
                            preview.getName(),
                            new ConjureCardOntoBattlefieldEffect(factory, Set.of(CardType.LAND)));
                })
                .toList();
        playerInputService.beginChooseModeChoice(
                gameData, entry.getControllerId(), entry.getCard(), new ChooseOneEffect(options),
                false, entry.getSourcePermanentId());
    }
}
