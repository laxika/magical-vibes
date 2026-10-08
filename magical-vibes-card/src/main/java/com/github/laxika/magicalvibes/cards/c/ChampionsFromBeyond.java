package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "11")
@CardRegistration(set = "FIC", collectorNumber = "101")
public class ChampionsFromBeyond extends Card {

    public ChampionsFromBeyond() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new XValue(), "Hero", 1, 1, null,
                List.of(CardSubtype.HERO), Set.of(), Set.of()));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(4),
                        SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1))), TriggerMode.INDEPENDENT);

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(8),
                        new BoostAllOwnCreaturesEffect(4, 4).forDeclaredAttackers()), TriggerMode.INDEPENDENT);
    }
}
