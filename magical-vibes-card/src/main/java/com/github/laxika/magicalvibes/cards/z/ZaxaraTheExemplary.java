package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1333")
public class ZaxaraTheExemplary extends Card {

    public ZaxaraTheExemplary() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2)),
                "{T}: Add two mana of any one color."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardHasXInManaCostPredicate(),
                List.of(new CreateXTokenWithXCountersEffect(
                        "Hydra", 0, 0,
                        CardColor.GREEN, Set.of(CardColor.GREEN),
                        List.of(CardSubtype.HYDRA), CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
