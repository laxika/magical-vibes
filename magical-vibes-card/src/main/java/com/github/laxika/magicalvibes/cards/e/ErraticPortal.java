package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "132")
@CardRegistration(set = "TPR", collectorNumber = "222")
public class ErraticPortal extends Card {

    public ErraticPortal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MayPayManaEffect(
                        "{1}",
                        null,
                        "Pay {1} to keep the target creature?",
                        MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                        ReturnToHandEffect.target(),
                        0
                )),
                "{1}, {T}: Return target creature to its owner's hand unless its controller pays {1}.",
                TargetFilters.creature()
        ));
    }
}
