package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "1")
public class AuroraEidolon extends Card {

    public AuroraEidolon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(), PreventDamageEffect.nextToTarget(3)),
                "{W}, Sacrifice this creature: Prevent the next 3 damage that would be dealt to any target this turn."
        ));

        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Aurora Eidolon from your graveyard to your hand?"))
        ));
    }
}
