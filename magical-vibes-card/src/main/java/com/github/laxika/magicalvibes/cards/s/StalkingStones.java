package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "327")
@CardRegistration(set = "MRD", collectorNumber = "284")
@CardRegistration(set = "TPR", collectorNumber = "245")
public class StalkingStones extends Card {

    public StalkingStones() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        // The animation lasts indefinitely (PERMANENT duration), so the artifact type is granted
        // persistently alongside the 3/3 body. It stays a land — no types are removed.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new AnimatePermanentsEffect(
                        3, 3,
                        List.of(CardSubtype.ELEMENTAL),
                        Set.of(),
                        null, Set.of(CardType.ARTIFACT),
                        GrantScope.SELF, EffectDuration.PERMANENT
                )),
                "{6}: Stalking Stones becomes a 3/3 Elemental artifact creature that's still a land."
        ));
    }
}
