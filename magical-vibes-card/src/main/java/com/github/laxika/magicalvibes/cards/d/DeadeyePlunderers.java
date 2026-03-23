package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "220")
public class DeadeyePlunderers extends Card {

    public DeadeyePlunderers() {
        // Deadeye Plunderers gets +1/+1 for each artifact you control.
        addEffect(EffectSlot.STATIC, new BoostSelfPerControlledPermanentEffect(1, 1,
                new PermanentIsArtifactPredicate()));

        // {2}{U}{B}: Create a Treasure token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{B}",
                List.of(CreateTokenEffect.ofArtifactToken(
                        1, "Treasure", List.of(CardSubtype.TREASURE),
                        List.of(new ActivatedAbility(
                                true, null,
                                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                "{T}, Sacrifice this artifact: Add one mana of any color."
                        ))
                )),
                "{2}{U}{B}: Create a Treasure token."
        ));
    }
}
