package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeArtifactCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyBecomeArtifactCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBecomeArtifactCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var change = (PerpetuallyBecomeArtifactCreatureEffect) effect;
        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Card modifiedCard = target.getCard().createRuntimeCopy();
        modifiedCard.setType(CardType.CREATURE);
        modifiedCard.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        modifiedCard.setPower(change.power());
        modifiedCard.setToughness(change.toughness());

        EnumSet<Keyword> keywords = modifiedCard.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(modifiedCard.getKeywords());
        keywords.add(Keyword.HASTE);
        modifiedCard.setKeywords(keywords);
        modifiedCard.freeze();
        target.exchangeCard(modifiedCard);

        gameLogService.append(gameData, GameLog.builder()
                .card(modifiedCard)
                .text(" perpetually becomes an artifact creature with base power and toughness "
                        + change.power() + "/" + change.toughness() + " and gains haste.")
                .build());
    }
}
