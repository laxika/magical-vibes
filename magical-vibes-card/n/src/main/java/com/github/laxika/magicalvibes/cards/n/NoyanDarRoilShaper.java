package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "216")
public class NoyanDarRoilShaper extends Card {

    public NoyanDarRoilShaper() {
        target(TargetFilters.landYouControl()).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new MayEffect(
                        new SpellCastTriggerEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))),
                                List.of(
                                        new PutCounterOnTargetPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 3),
                                        new AnimatePermanentsEffect(
                                                0, 0, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), null,
                                                Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.PERMANENT))),
                        "Put three +1/+1 counters on target land you control?"));
    }
}
