package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "DMU", collectorNumber = "114")
public class UrborgRepossession extends Card {

    public UrborgRepossession() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        CardIsPermanentPredicate permanent = new CardIsPermanentPredicate();

        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{G}"));

        target(new GraveyardCardPredicateTargetFilter(
                creature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(creature)
                        .targetGraveyard(true)
                        .build());
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));

        targetWhenKicked(new GraveyardCardPredicateTargetFilter(
                permanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 0, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new Kicked(), ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(permanent)
                                .targetGraveyard(true)
                                .build()));
    }
}
