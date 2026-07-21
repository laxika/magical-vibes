package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "178")
public class HostileDesert extends Card {

    public HostileDesert() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        // {2}, Exile a land card from your graveyard: This land becomes a 3/4 Elemental creature until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.LAND),
                        new AnimatePermanentsEffect(3, 4, List.of(CardSubtype.ELEMENTAL), Set.of())
                ),
                "{2}, Exile a land card from your graveyard: This land becomes a 3/4 Elemental creature until end of turn. It's still a land."
        ));
    }
}
