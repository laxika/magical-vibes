package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YTDM", collectorNumber = "11")
public class DragonsoulProdigy extends Card {

    public DragonsoulProdigy() {
        addTrigger(Triggers.youCastSpell(),
                new OncePerTurnTriggerEffect(new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.OMEN),
                        List.of(new ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect()))));
    }
}
