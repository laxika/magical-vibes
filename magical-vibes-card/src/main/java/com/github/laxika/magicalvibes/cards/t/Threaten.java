package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "242")
public class Threaten extends Card {

    public Threaten() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new UntapTargetPermanentEffect(Set.of(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new GainControlOfTargetCreatureUntilEndOfTurnEffect());
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, Scope.TARGET));
    }
}
