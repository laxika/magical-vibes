package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DSK", collectorNumber = "238")
public class VictorValgavothsSeneschal extends Card {

    public VictorValgavothsSeneschal() {
        SequenceEffect eerie = SequenceEffect.of(
                new ConditionalEffect(new NthAbilityResolutionThisTurn(1), new SurveilEffect(2)),
                new ConditionalEffect(new NthAbilityResolutionThisTurn(2),
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT)),
                new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .mandatory(true)
                                .build())
        );
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, eerie);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, eerie);
    }
}
