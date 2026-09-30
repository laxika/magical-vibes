package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "43")
@CardRegistration(set = "M3C", collectorNumber = "95")
public class SalvationColossus extends Card {

    public SalvationColossus() {
        var otherCreature = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new BoostAllOwnCreaturesEffect(2, 2, otherCreature));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES, otherCreature));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(8),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .grantHaste(true)
                                .exileAtEndStep(true)
                                .exileIfLeavesBattlefield(true)
                                .unearth(true)
                                .build()
                ),
                "Unearth — Pay eight {E}",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
