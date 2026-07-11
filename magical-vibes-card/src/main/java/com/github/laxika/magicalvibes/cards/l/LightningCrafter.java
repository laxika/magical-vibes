package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "93")
public class LightningCrafter extends Card {

    public LightningCrafter() {
        // Champion a Goblin or Shaman.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChampionCreatureEffect(CardSubtype.GOBLIN, CardSubtype.SHAMAN));
        // {T}: This creature deals 3 damage to any target.
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(3)),
                "{T}: Lightning Crafter deals 3 damage to any target."));
    }
}
