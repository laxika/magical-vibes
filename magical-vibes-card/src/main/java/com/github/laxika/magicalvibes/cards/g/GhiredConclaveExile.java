package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "42")
public class GhiredConclaveExile extends Card {

    public GhiredConclaveExile() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Rhino", 4, 4,
                CardColor.GREEN, List.of(CardSubtype.RHINO), Set.of(Keyword.TRAMPLE), Set.of()));
        addEffect(EffectSlot.ON_ATTACK, new PopulateEffect());
        addEffect(EffectSlot.ON_ATTACK, new MakeCreatedPermanentsAttackingEffect(true));
    }
}
