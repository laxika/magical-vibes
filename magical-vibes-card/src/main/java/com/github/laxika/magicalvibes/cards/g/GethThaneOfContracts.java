package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "95")
public class GethThaneOfContracts extends Card {

    public GethThaneOfContracts() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .exileIfLeavesBattlefield(true)
                        .build()),
                "{1}{B}{B}, {T}: Return target creature card from your graveyard to the battlefield. It gains \"If this creature would leave the battlefield, exile it instead of putting it anywhere else.\" Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
