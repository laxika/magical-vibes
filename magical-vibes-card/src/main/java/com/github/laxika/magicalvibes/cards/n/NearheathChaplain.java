package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "29")
public class NearheathChaplain extends Card {

    public NearheathChaplain() {
        // {2}{W}, Exile this card from your graveyard: Create two 1/1 white Spirit creature tokens
        // with flying. Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        CreateTokenEffect.whiteSpirit(2)
                ),
                "{2}{W}, Exile this card from your graveyard: Create two 1/1 white Spirit creature "
                        + "tokens with flying. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
