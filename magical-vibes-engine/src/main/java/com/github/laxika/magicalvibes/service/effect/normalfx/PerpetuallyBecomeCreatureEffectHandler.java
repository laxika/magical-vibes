package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;

/** Applies a perpetual creature type, subtype, and base power/toughness change to the source card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBecomeCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final UnattachTriggerSupport unattachTriggerSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBecomeCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBecomeCreatureEffect become = (PerpetuallyBecomeCreatureEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        boolean losesAttachmentSubtype = source.isAttached()
                && source.getCard().getSubtypes().stream()
                .anyMatch(subtype -> subtype == CardSubtype.EQUIPMENT || subtype == CardSubtype.AURA);
        Card copy = source.getCard().createRuntimeCopy();
        EnumSet<CardType> retainedTypes = EnumSet.noneOf(CardType.class);
        retainedTypes.add(copy.getType());
        retainedTypes.addAll(copy.getAdditionalTypes());
        retainedTypes.remove(CardType.CREATURE);
        copy.setType(CardType.CREATURE);
        copy.setAdditionalTypes(retainedTypes);
        ArrayList<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        if (!subtypes.contains(become.subtype())) {
            subtypes.add(become.subtype());
        }
        copy.setSubtypes(subtypes);
        copy.setPower(become.power());
        copy.setToughness(become.toughness());
        copy.freeze();
        source.exchangeCard(copy);

        if (losesAttachmentSubtype) {
            unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, source, source.getAttachedTo());
            source.setAttachedTo(null);
            gameData.expireFloatingEffectsForUnattachedSource(source.getId());
        }

        gameLogService.append(gameData, GameLog.cardThen(copy,
                " perpetually becomes a " + become.power() + "/" + become.toughness() + " "
                        + become.subtype().getDisplayName() + " creature in addition to its other types."));
    }
}
