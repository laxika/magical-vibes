package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "162")
@CardRegistration(set = "KLR", collectorNumber = "249")
@CardRegistration(set = "MKC", collectorNumber = "231")
@CardRegistration(set = "LCC", collectorNumber = "307")
@CardRegistration(set = "C20", collectorNumber = "244")
@CardRegistration(set = "MIC", collectorNumber = "160")
public class LifecraftersBestiary extends Card {

    public LifecraftersBestiary() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(1));
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new MayPayManaEffect("{G}", new DrawCardEffect(1), "Pay {G} to draw a card?"))
        ));
    }
}
