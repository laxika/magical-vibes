package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardOneManaOfEachColorAmongCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

@Component
public class AwardOneManaOfEachColorAmongCardsExiledWithSourceEffectHandler
        implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardOneManaOfEachColorAmongCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        EnumSet<CardColor> colors = EnumSet.noneOf(CardColor.class);
        gameData.getCardsExiledByPermanent(permanent.getId()).forEach(card -> colors.addAll(card.getColors()));

        ManaPool pool = gameData.playerManaPools.get(playerId);
        for (CardColor color : colors) {
            ManaColor manaColor = ManaProductionSupport.effectiveColor(
                    gameData, playerId, ManaColor.valueOf(color.name()));
            pool.add(manaColor, manaMultiplier);
            if (creatureSource) {
                pool.addCreatureMana(manaColor, manaMultiplier);
            }
        }
    }
}
