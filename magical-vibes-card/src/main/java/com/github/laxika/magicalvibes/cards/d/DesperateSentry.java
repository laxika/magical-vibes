package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "21")
public class DesperateSentry extends Card {

    public DesperateSentry() {
        // When this creature dies, create a 3/2 colorless Eldrazi Horror creature token.
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Eldrazi Horror", 3, 2, null,
                List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), false
        ));

        // Delirium — This creature gets +3/+0 as long as there are four or more card types among
        // cards in your graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new StaticBoostEffect(3, 0, GrantScope.SELF)));
    }
}
