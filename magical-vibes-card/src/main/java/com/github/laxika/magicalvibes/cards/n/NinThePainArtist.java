package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMD", collectorNumber = "213")
public class NinThePainArtist extends Card {

    public NinThePainArtist() {
        // {X}{U}{R}, {T}: Nin deals X damage to target creature. That creature's controller draws X cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{U}{R}",
                List.of(
                        new DealDamageToTargetCreatureEffect(new XValue()),
                        new TargetPermanentControllerDrawsCardEffect(new XValue())),
                "{X}{U}{R}, {T}: Nin deals X damage to target creature. That creature's controller draws X cards.",
                TargetFilters.creature()));
    }
}
