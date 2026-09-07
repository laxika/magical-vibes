package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "119")
public class ForbiddenFriendship extends Card {

    public ForbiddenFriendship() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Dinosaur", 1, 1, CardColor.RED, List.of(CardSubtype.DINOSAUR),
                Set.of(Keyword.HASTE), Set.of()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
