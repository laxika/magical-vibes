package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaUpToNTimesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "130")
public class HawkeyeMasterMarksman extends Card {

    public HawkeyeMasterMarksman() {
        CantBlockThisTurnEffect net = new CantBlockThisTurnEffect(TapUntapScope.TARGET);
        DealDamageToTargetPlayerOrPlaneswalkerEffect explosive =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2);
        SequenceEffect boomerang = SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                new DrawCardEffect(1));

        target(TargetFilters.creature(), 0, 1);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"
        ), 0, 1);
        registerEffectTargetIndex(net, 0);
        registerEffectTargetIndex(explosive, 1);

        ChooseOneEffect arrowModes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Net", net),
                new ChooseOneEffect.ChooseOneOption("Explosive", explosive),
                new ChooseOneEffect.ChooseOneOption("Boomerang", boomerang)
        ), false, 0, 3, false);

        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        new PayManaUpToNTimesEffect("{1}", 3, arrowModes)));
    }
}
