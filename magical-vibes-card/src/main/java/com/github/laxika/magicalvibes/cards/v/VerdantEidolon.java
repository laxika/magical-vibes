package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "100")
public class VerdantEidolon extends Card {

    public VerdantEidolon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(3)),
                "{G}, Sacrifice this creature: Add three mana of any one color."
        ));

        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(new MayEffect(
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Return Verdant Eidolon from your graveyard to your hand?"))
        ));
    }
}
