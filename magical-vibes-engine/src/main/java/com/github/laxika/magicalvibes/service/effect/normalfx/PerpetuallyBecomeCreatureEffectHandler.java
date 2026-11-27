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
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** "It perpetually becomes a 2/1 Rat creature in addition to its other types": the source permanent's card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBecomeCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DurationClock durationClock;
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
        Card physical = source.getOriginalCard() != null ? source.getOriginalCard() : source.getCard();
        become(gameData, physical.getId(), become.power(), become.toughness(), become.subtype());

        if (losesAttachmentSubtype) {
            unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, source, source.getAttachedTo());
            source.setAttachedTo(null);
            durationClock.sourceUnattached(gameData, source.getId());
        }

        gameLogService.append(gameData, GameLog.cardThen(physical,
                " perpetually becomes a " + become.power() + "/" + become.toughness() + " "
                        + become.subtype().getDisplayName() + " creature in addition to its other types."));
    }

    /**
     * Records that the physical card {@code cardId} perpetually is a {@code power}/{@code toughness} {@code subtype}
     * creature in addition to its other types, in the perpetual stores every zone reads: the types and subtypes
     * seeded as its characteristics, the base power and toughness a layer-7b setter with this timestamp
     * (CR 613.4b), none of it a copiable value (CR 707.2).
     */
    public static void become(GameData gameData, UUID cardId, int power, int toughness, CardSubtype subtype) {
        PerpetuallyGrantCardCharacteristicsEffectHandler.grant(gameData, cardId,
                new PerpetuallyGrantCardCharacteristicsEffect(Set.of(CardType.CREATURE), Set.of(subtype), List.of()));
        gameData.perpetualCardBasePowerToughness.put(cardId,
                new GameData.PerpetualBasePowerToughness(power, toughness, gameData.nextTimestamp()));
    }
}
