package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MUL", collectorNumber = "56")
@CardRegistration(set = "MUL", collectorNumber = "121")
@CardRegistration(set = "MUL", collectorNumber = "186")
@CardRegistration(set = "DMU", collectorNumber = "212")
public class RaffWeatherlightStalwart extends Card {

    public RaffWeatherlightStalwart() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                instantOrSorcery,
                List.of(new MayPayTapPermanentsEffect(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect(1),
                        "Tap two untapped creatures you control to draw a card?"))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES)),
                "{3}{W}{W}: Creatures you control get +1/+1 and gain vigilance until end of turn."
        ));
    }
}
