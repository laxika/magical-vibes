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
public class FerventPaincaster extends Card {

    public FerventPaincaster() {
        // {T}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: Fervent Paincaster deals 1 damage to target player or planeswalker."));

        // {T}, Exert this creature: It deals 1 damage to target creature. Exert is the extra cost of
        // keeping the creature tapped through its next untap step (SkipNextUntapEffect, SELF).
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetCreatureEffect(1), new SkipNextUntapEffect(TapUntapScope.SELF)),
                "{T}, Exert Fervent Paincaster: It deals 1 damage to target creature."));
    }
}
