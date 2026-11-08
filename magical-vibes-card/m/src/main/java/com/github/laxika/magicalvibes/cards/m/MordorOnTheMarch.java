package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "512")
@CardRegistration(set = "LTC", collectorNumber = "556")
public class MordorOnTheMarch extends Card {

    public MordorOnTheMarch() {
        addSpellEffect(new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                new CardTypePredicate(CardType.CREATURE),
                true,
                List.of(),
                true,
                true));
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
