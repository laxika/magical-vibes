package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsCantBeCounteredEffect;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "59")
public class LierDiscipleOfTheDrowned extends Card {

    public LierDiscipleOfTheDrowned() {
        addEffect(EffectSlot.STATIC, new SpellsCantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new GrantFlashbackToGraveyardCardsEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
