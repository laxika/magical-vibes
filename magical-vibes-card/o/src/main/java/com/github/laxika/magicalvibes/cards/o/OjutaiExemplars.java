package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "27")
public class OjutaiExemplars extends Card {

    public OjutaiExemplars() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Tap target creature",
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                TargetFilters.creature()),
                        new ChooseOneEffect.ChooseOneOption(
                                "Ojutai Exemplars gains first strike and lifelink until end of turn",
                                new GrantKeywordEffect(
                                        Set.of(Keyword.FIRST_STRIKE, Keyword.LIFELINK), GrantScope.SELF)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Exile Ojutai Exemplars, then return it to the battlefield tapped under its owner's control",
                                new FlickerEffect(
                                        FlickerScope.SELF,
                                        null,
                                        ReturnTiming.IMMEDIATE,
                                        TurnStep.END_STEP,
                                        true,
                                        null,
                                        null,
                                        0,
                                        false,
                                        false))
                )))
        ));
    }
}
