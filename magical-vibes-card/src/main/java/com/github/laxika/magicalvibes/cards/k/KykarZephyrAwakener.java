package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "122")
public class KykarZephyrAwakener extends Card {

    public KykarZephyrAwakener() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Exile another target creature you control. Return that card at the beginning of the next end step",
                                FlickerEffect.exileTargetReturnAtEndStep(),
                                new PermanentPredicateTargetFilter(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentControlledBySourceControllerPredicate(),
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                        )),
                                        "Target must be another creature you control"
                                )),
                        new ChooseOneEffect.ChooseOneOption(
                                "Create a 1/1 white Spirit creature token with flying",
                                CreateTokenEffect.whiteSpirit(1)
                        )
                )))
        ));
    }
}
