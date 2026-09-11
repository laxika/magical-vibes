package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "209")
public class VarisSilverymoonRanger extends Card {

    public VarisSilverymoonRanger() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER))),
                        List.of(new VentureIntoDungeonEffect()))));

        addEffect(EffectSlot.ON_CONTROLLER_COMPLETES_DUNGEON,
                new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.WOLF), Set.of(), Set.of()));
    }
}
