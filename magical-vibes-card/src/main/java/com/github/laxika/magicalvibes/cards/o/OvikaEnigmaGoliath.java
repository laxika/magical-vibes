package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "213")
public class OvikaEnigmaGoliath extends Card {

    public OvikaEnigmaGoliath() {
        CreateTokenEffect phyrexianGoblin = new CreateTokenEffect(
                CardType.CREATURE, new EventValue(), "Phyrexian Goblin", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN), Set.of(), Set.of(), false, false,
                java.util.Map.of(), List.of(), false, false, false, 0, Set.of(Keyword.HASTE));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new CreateTokenForTriggeringPlayerEffect(phyrexianGoblin))
        ));
    }
}
