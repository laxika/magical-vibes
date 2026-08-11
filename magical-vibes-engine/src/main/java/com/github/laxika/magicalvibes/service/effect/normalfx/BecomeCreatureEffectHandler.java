package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Resolves {@link BecomeCreatureEffect} by replacing the source permanent's frozen card with a
 * mutable runtime copy whose only card type is creature.
 */
@Component
@RequiredArgsConstructor
public class BecomeCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomeCreatureEffect becomeCreature = (BecomeCreatureEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !gameQueryService.isEnchantment(gameData, source)) {
            return;
        }

        Card copy = source.getCard().createRuntimeCopy();
        copy.setType(CardType.CREATURE);
        copy.setAdditionalTypes(Set.of());
        copy.setSubtypes(becomeCreature.subtypes());
        copy.setPower(becomeCreature.power());
        copy.setToughness(becomeCreature.toughness());
        EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        keywords.addAll(copy.getKeywords());
        keywords.addAll(becomeCreature.keywords());
        copy.setKeywords(keywords);
        copy.freeze();
        source.setCard(copy);

        gameLogService.append(gameData, GameLog.cardThen(copy,
                " becomes a " + becomeCreature.power() + "/" + becomeCreature.toughness() + " creature."));
    }
}
