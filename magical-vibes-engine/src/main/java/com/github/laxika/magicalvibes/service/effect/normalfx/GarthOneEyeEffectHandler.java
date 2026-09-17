package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GarthOneEyeCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GarthOneEyeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Presents Garth One-Eye's six named cards, excluding names already chosen by that permanent. */
@Component
@RequiredArgsConstructor
public class GarthOneEyeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GarthOneEyeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GarthOneEyeEffect garthEffect = (GarthOneEyeEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Set<String> alreadyChosen = source == null ? Set.of() : source.getChosenModeLabels();
        List<ChooseOneEffect.ChooseOneOption> options = garthEffect.cards().stream()
                .filter(card -> !alreadyChosen.contains(card.getName()))
                .map(card -> new ChooseOneEffect.ChooseOneOption(
                        card.getName(), new GarthOneEyeCopyEffect(card)))
                .toList();

        if (options.isEmpty()) {
            return;
        }

        playerInputService.beginChooseModeChoice(
                gameData,
                entry.getControllerId(),
                entry.getCard(),
                new ChooseOneEffect(options),
                false,
                entry.getSourcePermanentId(),
                alreadyChosen.stream().toList());
    }
}
