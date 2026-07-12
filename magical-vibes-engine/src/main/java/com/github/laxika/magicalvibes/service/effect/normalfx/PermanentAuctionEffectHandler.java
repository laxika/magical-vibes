package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentAuctionEffect;
import com.github.laxika.magicalvibes.service.PermanentAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentAuctionEffectHandler implements NormalEffectHandlerBean {

    private final PermanentAuctionService permanentAuctionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PermanentAuctionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        permanentAuctionService.beginAuction(gameData, entry.getControllerId(), entry.getCard().getName());
    }
}
