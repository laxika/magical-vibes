package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "31")
public class KorSpiritdancer extends Card {

    public KorSpiritdancer() {
        // This creature gets +2/+2 for each Aura attached to it.
        Scaled twicePerAura = new Scaled(new AttachmentsOnSource(true, false), 2);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(twicePerAura, twicePerAura));

        // Whenever you cast an Aura spell, you may draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardIsAuraPredicate(), List.of(new DrawCardEffect())),
                "Draw a card?"
        ));
    }
}
