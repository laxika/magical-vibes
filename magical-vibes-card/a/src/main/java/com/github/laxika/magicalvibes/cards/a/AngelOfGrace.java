package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "1")
public class AngelOfGrace extends Card {

    public AngelOfGrace() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DamageLifeFloorUntilEndOfTurnEffect(1));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}{W}",
                List.of(new ExileSelfFromGraveyardCost(), new SetLifeTotalEffect(10)),
                "{4}{W}{W}, Exile this card from your graveyard: Your life total becomes 10."
        ));
    }
}
