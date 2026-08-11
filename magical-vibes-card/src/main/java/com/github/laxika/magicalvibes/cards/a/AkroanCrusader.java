package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "111")
public class AkroanCrusader extends Card {

    public AkroanCrusader() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new CreateTokenEffect(1, "Soldier", 1, 1, CardColor.RED,
                        List.of(CardSubtype.SOLDIER), Set.of(Keyword.HASTE), Set.of())),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
