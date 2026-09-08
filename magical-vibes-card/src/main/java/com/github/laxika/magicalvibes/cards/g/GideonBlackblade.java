package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "13")
public class GideonBlackblade extends Card {

    public GideonBlackblade() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                        Set.of(Keyword.INDESTRUCTIBLE))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new PreventAllDamageEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Vigilance",
                                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET)),
                        new ChooseOneEffect.ChooseOneOption("Lifelink",
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                        new ChooseOneEffect.ChooseOneOption("Indestructible",
                                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET))
                ))),
                "+1: Up to one other target creature you control gains your choice of vigilance, "
                        + "lifelink, or indestructible until end of turn.",
                null,
                +1,
                null,
                null,
                List.<TargetFilter>of(otherCreatureYouControl()),
                0,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new ExileTargetPermanentEffect()),
                "-6: Exile target nonland permanent.",
                TargetFilters.nonlandPermanent()
        ));
    }

    private static TargetFilter otherCreatureYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature you control");
    }
}
