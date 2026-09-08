package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "186")
@CardRegistration(set = "ECL", collectorNumber = "327")
public class MutableExplorer extends Card {

    public MutableExplorer() {
        CreateTokenEffect mutavaultToken = new CreateTokenEffect(
                CardType.LAND,
                1,
                "Mutavault",
                0,
                0,
                null,
                null,
                List.of(),
                Set.of(),
                Set.of(),
                false,
                true,
                Map.of(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS)),
                List.of(new ActivatedAbility(
                        false,
                        "{1}",
                        List.of(new AnimatePermanentsEffect(2, 2, List.of(), Set.of(Keyword.CHANGELING))),
                        "{1}: This token becomes a 2/2 creature with all creature types until end of turn. It's still a land."
                )),
                false,
                false,
                false,
                0,
                Set.of()
        );

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, mutavaultToken);
    }
}
