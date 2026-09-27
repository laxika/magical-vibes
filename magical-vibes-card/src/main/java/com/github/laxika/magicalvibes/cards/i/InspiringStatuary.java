package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "AER", collectorNumber = "160")
@CardRegistration(set = "HA4", collectorNumber = "23")
@CardRegistration(set = "BRR", collectorNumber = "22")
@CardRegistration(set = "CMM", collectorNumber = "394")
@CardRegistration(set = "CMM", collectorNumber = "609")
public class InspiringStatuary extends Card {

    public InspiringStatuary() {
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToSpellsEffect(
                Keyword.IMPROVISE, new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT))));
    }
}
