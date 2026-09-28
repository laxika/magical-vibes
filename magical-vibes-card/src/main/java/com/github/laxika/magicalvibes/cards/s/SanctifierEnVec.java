package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsOfColorsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "27")
public class SanctifierEnVec extends Card {

    public SanctifierEnVec() {
        Set<CardColor> protectedColors = Set.of(CardColor.BLACK, CardColor.RED);
        CardAnyOfPredicate blackOrRed = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(CardColor.BLACK),
                new CardColorPredicate(CardColor.RED)
        ));

        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(protectedColors));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(0, GraveyardExileScope.ALL_PLAYERS, blackOrRed));
        addEffect(EffectSlot.STATIC, new ExileCardsOfColorsInsteadOfGraveyardEffect(protectedColors));
    }
}
