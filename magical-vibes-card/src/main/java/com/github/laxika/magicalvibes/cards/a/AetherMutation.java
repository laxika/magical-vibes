package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "91")
public class AetherMutation extends Card {

    public AetherMutation() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect(
                        new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())));
    }
}
