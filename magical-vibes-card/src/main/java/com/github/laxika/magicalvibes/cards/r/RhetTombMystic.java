package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DRC", collectorNumber = "10")
@CardRegistration(set = "DRC", collectorNumber = "26")
public class RhetTombMystic extends Card {

    public RhetTombMystic() {
        ActivatedAbility cycling = new ActivatedAbility(false, "{1}{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {1}{U} ({1}{U}, Discard this card: Draw a card.)");
        addEffect(EffectSlot.STATIC, new GrantHandActivatedAbilityToCardsEffect(
                cycling, new CardTypePredicate(CardType.CREATURE), true));
    }
}
