package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "82")
public class NecropolisFiend extends Card {

    public NecropolisFiend() {
        addEffect(EffectSlot.SPELL, new DelveCost());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new ExileXCardsFromGraveyardCost(),
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))
                ),
                "{X}, {T}, Exile X cards from your graveyard: Target creature gets -X/-X until end of turn.",
                TargetFilters.creature()
        ));
    }
}
