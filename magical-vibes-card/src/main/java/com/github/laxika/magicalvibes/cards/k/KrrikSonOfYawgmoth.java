package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeForBlackManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayBlackManaWithLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1186")
@CardRegistration(set = "SLD", collectorNumber = "1204")
@CardRegistration(set = "FCA", collectorNumber = "36")
public class KrrikSonOfYawgmoth extends Card {

    public KrrikSonOfYawgmoth() {
        addEffect(EffectSlot.STATIC, new PayBlackManaWithLifeEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.BLACK),
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
