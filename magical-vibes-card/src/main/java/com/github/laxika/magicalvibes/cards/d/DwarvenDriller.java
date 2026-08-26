package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "85")
public class DwarvenDriller extends Card {

    public DwarvenDriller() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentUnlessControllerTakesDamageEffect(2)),
                "{T}: Destroy target land unless its controller has this creature deal 2 damage to them.",
                TargetFilters.land()));
    }
}
