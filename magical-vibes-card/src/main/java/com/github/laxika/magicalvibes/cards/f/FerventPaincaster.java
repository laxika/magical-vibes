package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "91")
@CardRegistration(set = "AKR", collectorNumber = "153")
public class FerventPaincaster extends Card {

    public FerventPaincaster() {
        // {T}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: Fervent Paincaster deals 1 damage to target player or planeswalker."));

        // {T}, Exert this creature: It deals 1 damage to target creature. Exert is paid as part of
        // the activation cost (SkipNextUntapEffect with activationCost).
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetCreatureEffect(1), new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true, true)),
                "{T}, Exert Fervent Paincaster: It deals 1 damage to target creature."));
    }
}
