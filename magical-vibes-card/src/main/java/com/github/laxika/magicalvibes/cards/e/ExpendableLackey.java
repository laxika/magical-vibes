package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "43")
public class ExpendableLackey extends Card {

    public ExpendableLackey() {
        CreateTokenEffect fish = new CreateTokenEffect(
                1,
                "Fish",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FISH),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBeBlockedEffect())
        );

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new ExileSelfFromGraveyardCost(), fish),
                "{1}{U}, Exile this card from your graveyard: Create a 1/1 blue Fish creature token with \"This token can't be blocked.\" Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
