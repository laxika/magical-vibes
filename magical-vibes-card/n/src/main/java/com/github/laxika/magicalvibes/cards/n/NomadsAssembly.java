package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "39")
@CardRegistration(set = "C14", collectorNumber = "82")
public class NomadsAssembly extends Card {

    public NomadsAssembly() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                "Kor Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KOR, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
