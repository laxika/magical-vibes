package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "78")
public class GateToManorborn extends Card {

    public GateToManorborn() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(new SeekEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                "{3}{G}, {T}: Seek a nonland card. Activate only once."
        ).withMaxActivationsPerGame(1));
    }
}
