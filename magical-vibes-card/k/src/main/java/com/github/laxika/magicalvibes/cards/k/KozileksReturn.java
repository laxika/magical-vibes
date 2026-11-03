package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "98")
@CardRegistration(set = "PIO", collectorNumber = "142")
@CardRegistration(set = "M3C", collectorNumber = "214")
public class KozileksReturn extends Card {

    public KozileksReturn() {
        addSpellEffect(new MassDamageEffect(2));

        addTrigger(new TriggerCondition(Triggers.youCastSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.ELDRAZI),
                                new CardMinManaValuePredicate(7)
                        ))), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new ConditionalEffect(
                        new SourceCardInGraveyard(),
                        new MayEffect(
                                SequenceEffect.of(
                                        new ExileSourceCardFromGraveyardEffect(),
                                        new MassDamageEffect(5)),
                                "Exile Kozilek's Return from your graveyard?")));
    }
}
