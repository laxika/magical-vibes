package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekDragonAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SeekDragonAndPerpetuallyBoostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekDragonAndPerpetuallyBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekDragonAndPerpetuallyBoostEffect seek = (SeekDragonAndPerpetuallyBoostEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        Card found = null;
        if (library != null) {
            List<Card> dragons = library.stream()
                    .filter(card -> card.getSubtypes().contains(CardSubtype.DRAGON))
                    .toList();
            if (!dragons.isEmpty()) {
                found = dragons.get(ThreadLocalRandom.current().nextInt(dragons.size()));
                library.remove(found);
                gameData.playerHands.get(controllerId).add(found);
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        }

        PerpetualCardPowerToughnessSupport.remember(
                gameData, entry.getCard(), seek.powerBoost(), seek.toughnessBoost());
        if (found != null) {
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, found, seek.powerBoost(), seek.toughnessBoost());
        }

        if (entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                PerpetualCardPowerToughnessSupport.applyToPermanent(
                        gameData, entry.getControllerId(), source,
                        seek.powerBoost(), seek.toughnessBoost());
            }
        }
    }
}
