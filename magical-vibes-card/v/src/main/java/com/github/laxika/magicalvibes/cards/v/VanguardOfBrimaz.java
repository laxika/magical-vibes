package com.github.laxika.magicalvibes.cards.v;

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

@CardRegistration(set = "BNG", collectorNumber = "29")
public class VanguardOfBrimaz extends Card {

    public VanguardOfBrimaz() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new CreateTokenEffect(
                        "Cat Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.CAT, CardSubtype.SOLDIER),
                        Set.of(Keyword.VIGILANCE), Set.of())),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
