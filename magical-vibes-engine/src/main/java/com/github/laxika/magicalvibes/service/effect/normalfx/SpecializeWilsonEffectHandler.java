package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.w.WilsonBearComrade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeWilsonEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Applies Wilson's five digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeWilsonEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeWilsonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var specialize = (SpecializeWilsonEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Wilson, Bear Comrade".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        setBaseFaceCharacteristics(specialized);
        WilsonBearComrade.setFaceCharacteristics(specialized, specialize.color());
        WilsonBearComrade.addWardAbility(specialized);
        if (specialize.color() == CardColor.BLUE) {
            specialized.addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        }
        specialized.addGraveyardActivatedAbility(
                WilsonBearComrade.graveyardAbility(specialize.color()));
        source.exchangeCard(specialized);
    }

    private void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("19");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.BEAR, CardSubtype.WARRIOR));
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        card.setColorIdentity(List.of(CardColor.GREEN));
        card.setKeywords(Set.of());
    }
}
