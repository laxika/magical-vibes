package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PC2", collectorNumber = "87")
@CardRegistration(set = "PCA", collectorNumber = "87")
public class DragonlairSpider extends Card {

    public DragonlairSpider() {
        // Whenever an opponent casts a spell, create a 1/1 green Insect creature token.
        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastTriggerEffect(null, List.of(new CreateTokenEffect(
                        "Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of()))));
    }
}
