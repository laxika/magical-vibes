package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToReturnedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2450")
public class PreWarFormalwear extends Card {

    public PreWarFormalwear() {
        var creatureCard = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(3)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(creatureCard)
                        .targetGraveyard(true)
                        .build(),
                new AttachSourceEquipmentToReturnedPermanentEffect()));
        addEffect(EffectSlot.STATIC,
                new AttachedBoostEffect(new Fixed(2), new Fixed(2), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
