package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "123")
public class Bookwurm extends Card {

    public Bookwurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2)),
                "{2}{G}: Put this card from your graveyard into your library third from the top."
        ));
    }
}
