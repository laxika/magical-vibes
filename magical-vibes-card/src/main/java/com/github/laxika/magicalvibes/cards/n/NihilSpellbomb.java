package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "187")
public class NihilSpellbomb extends Card {

    public NihilSpellbomb() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new ExileTargetPlayerGraveyardEffect()),
                "{T}, Sacrifice Nihil Spellbomb: Exile target player's graveyard."
        ));

        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{B}", new DrawCardEffect(1), "Pay {B} to draw a card?"));
    }
}
