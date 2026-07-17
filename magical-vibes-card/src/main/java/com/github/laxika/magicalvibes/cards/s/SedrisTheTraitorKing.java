package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "193")
public class SedrisTheTraitorKing extends Card {

    public SedrisTheTraitorKing() {
        // Each creature card in your graveyard has unearth {2}{B}.
        // (Unearth {2}{B}: Return the card to the battlefield. It gains haste. Exile it at the
        // beginning of the next end step or if it would leave the battlefield. Unearth only as a sorcery.)
        ActivatedAbility unearth = new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .grantHaste(true)
                        .exileAtEndStep(true)
                        .build()),
                "Unearth {2}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        );

        addEffect(EffectSlot.STATIC, new GrantGraveyardAbilityToCreatureCardsEffect(unearth));
    }
}
