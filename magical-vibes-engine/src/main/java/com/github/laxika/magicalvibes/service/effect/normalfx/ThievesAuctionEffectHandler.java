package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ThievesAuctionEffect;
import com.github.laxika.magicalvibes.service.ThievesAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThievesAuctionEffectHandler implements NormalEffectHandlerBean {

    private final ThievesAuctionService thievesAuctionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ThievesAuctionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        thievesAuctionService.beginAuction(gameData, entry.getControllerId(), entry.getCard().getName());
    }
}
