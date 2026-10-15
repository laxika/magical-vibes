package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "18")
public class SurpriseDeployment extends Card {

    public SurpriseDeployment() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.ONLY_DURING_COMBAT);

        CardAllOfPredicate nonwhiteCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardColorPredicate(CardColor.WHITE))));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldEffect(nonwhiteCreature, "nonwhite creature")
                        .returningToHandAtEndStep(),
                "Put a nonwhite creature card from your hand onto the battlefield?"));
    }
}
