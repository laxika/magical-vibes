package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "3")
public class AuspiciousAncestor extends Card {

    public AuspiciousAncestor() {
        // When this creature dies, you gain 3 life.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(3));

        // Whenever a player casts a white spell, you may pay {1}. If you do, you gain 1 life.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.WHITE),
                List.of(new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?"))));
    }
}
