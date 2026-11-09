package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffectHandler
        implements NormalEffectHandlerBean {

    private final DealDamageToTargetPlayerOrPlaneswalkerEffectHandler delegate;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "Have it deal …": the permanent the enter event was about deals the damage, as it is now or as it last existed
        // on the battlefield (CR 608.2h); an ability with no such permanent (a creature's own "when this enters") is
        // its source's.
        Card dealer = triggeringPermanentCard(gameData, entry);
        if (dealer != null) {
            entry.setDamageSourceCard(dealer);
            var e = (EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect) effect;
            delegate.resolve(gameData, entry, new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                    e.amount(), e.playerRelation()));
            return;
        }
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source != null) {
            entry.setDamageSourceCard(source.getCard());
        }

        var e = (EnteringPermanentDealsDamageToTargetPlayerOrPlaneswalkerEffect) effect;
        delegate.resolve(gameData, entry, new DealDamageToTargetPlayerOrPlaneswalkerEffect(
                e.amount(), e.playerRelation()));
    }

    private Card triggeringPermanentCard(GameData gameData, StackEntry entry) {
        if (entry.getTriggeringPermanentId() == null) {
            return null;
        }
        Permanent live = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (live != null) {
            return live.getCard();
        }
        if (entry.getTriggeringPermanentLastKnownCard() != null) {
            return entry.getTriggeringPermanentLastKnownCard();
        }
        Card lastKnown = entry.lastKnownPermanentCard(entry.getTriggeringPermanentId());
        return lastKnown != null ? lastKnown : entry.getTriggeringCardSnapshot();
    }
}
