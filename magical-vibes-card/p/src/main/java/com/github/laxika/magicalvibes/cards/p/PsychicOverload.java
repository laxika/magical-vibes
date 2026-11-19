package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "28")
public class PsychicOverload extends Card {

    public PsychicOverload() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(Subjects.attached()))
                .addStaticEffect(DoesntUntapEffect.enchanted())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                null,
                                List.of(
                                        new DiscardCardTypeCost(new CardTypePredicate(CardType.ARTIFACT), "artifact", 2),
                                        new UntapPermanentsEffect(Subjects.source())
                                ),
                                "Discard two artifact cards: Untap this permanent."
                        ),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
