package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "98")
public class KozileksReturn extends Card {

    public KozileksReturn() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2));

        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.ELDRAZI),
                        new CardMinManaValuePredicate(7)
                )),
                List.of(new ConditionalEffect(
                        new SourceCardInGraveyard(),
                        new MayEffect(
                                SequenceEffect.of(
                                        new ExileSourceCardFromGraveyardEffect(),
                                        new MassDamageEffect(5)),
                                "Exile Kozilek's Return from your graveyard?")))
        ));
    }
}
