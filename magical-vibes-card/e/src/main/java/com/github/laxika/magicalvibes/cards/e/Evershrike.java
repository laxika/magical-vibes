package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "88")
public class Evershrike extends Card {

    public Evershrike() {
        // Evershrike gets +2/+2 for each Aura attached to it.
        Scaled twicePerAura = new Scaled(new AttachmentsOnSource(true, false), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerAura, twicePerAura));

        // {X}{W/B}{W/B}: Return this card from your graveyard to the battlefield. You may put an Aura
        // card with mana value X or less from your hand onto the battlefield attached to it. If you
        // don't, exile this creature.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{X}{W/B}{W/B}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        new PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect()),
                "{X}{W/B}{W/B}: Return this card from your graveyard to the battlefield. You may put an Aura card with mana value X or less from your hand onto the battlefield attached to it. If you don't, exile this creature."
        ));
    }
}
