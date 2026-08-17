package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "100")
public class BehindTheScenes extends Card {

    public BehindTheScenes() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SKULK, GrantScope.OWN_CREATURES));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{4}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
