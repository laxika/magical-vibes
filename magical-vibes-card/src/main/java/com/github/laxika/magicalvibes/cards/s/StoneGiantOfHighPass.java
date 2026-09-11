package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "113")
public class StoneGiantOfHighPass extends Card {

    public StoneGiantOfHighPass() {
        CreateTokenEffect stoneBoulder = new CreateTokenEffect(
                "Stone Boulder", 3, 1, null,
                List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, stoneBoulder);
        addEffect(EffectSlot.ON_ATTACK, stoneBoulder);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new DealDamageToAnyTargetEffect(4)
                ),
                "{2}{R}, Sacrifice an artifact: This creature deals 4 damage to any target."
        ));
    }
}
