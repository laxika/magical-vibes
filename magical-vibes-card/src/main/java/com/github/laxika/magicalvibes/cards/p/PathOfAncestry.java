package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "158")
public class PathOfAncestry extends Card {

    public PathOfAncestry() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.COMMANDER_COLOR_IDENTITY)),
                "{T}: Add one mana of any color in your commander's color identity."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.usingManaProducedBySource(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSharesCreatureTypeWithCommanderPredicate()
                        )),
                        List.of(new ScryEffect(1))
                ));
    }
}
