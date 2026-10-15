package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "145")
public class WirewoodHivemaster extends Card {

    public WirewoodHivemaster() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(new CardSubtypePredicate(CardSubtype.ELF),
                                new CardNotPredicate(new CardIsTokenPredicate()))),
                        new MayEffect(
                                new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                                        List.of(CardSubtype.INSECT), Set.of(), Set.of()),
                                "Create a 1/1 green Insect creature token?")));
    }
}
