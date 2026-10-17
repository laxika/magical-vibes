package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfCyclicalTime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "67")
@CardRegistration(set = "LCI", collectorNumber = "315")
public class OjerPakpatiq extends Card {

    public OjerPakpatiq() {
        setBackFaceCard(new TempleOfCyclicalTime());

        // Whenever you cast an instant spell from your hand, it gains rebound.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.INSTANT),
                List.of(new GrantKeywordsToCastSpellEffect(Set.of(Keyword.REBOUND))),
                null,
                null,
                new StackEntryCastFromZonePredicate(Zone.HAND),
                false,
                false,
                null,
                0));
        addEffect(EffectSlot.ON_DEATH,
                new ReturnSourceTransformedFromGraveyardEffect(true, CounterType.TIME, 3, true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "TempleOfCyclicalTime";
    }
}
