package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "240")
public class FrostwalkBastion extends Card {

    public FrostwalkBastion() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        // {1}{S}: Until end of turn, this land becomes a 2/3 Construct artifact creature. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(new AnimatePermanentsEffect(
                        2, 3, List.of(CardSubtype.CONSTRUCT), Set.of(), null, Set.of(CardType.ARTIFACT)
                )),
                "{1}{S}: Until end of turn, this land becomes a 2/3 Construct artifact creature. It's still a land."
        ));

        // Whenever this land deals combat damage to a creature, tap that creature and it doesn't untap during
        // its controller's next untap step.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
