package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "194")
public class DreyKeeper extends Card {

    public DreyKeeper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Squirrel", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()));

        PermanentHasSubtypePredicate squirrels = new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 0, squirrels),
                        new GrantKeywordEffect(Set.of(Keyword.MENACE), GrantScope.OWN_CREATURES, squirrels)
                ),
                "{3}{B}: Squirrels you control get +1/+0 and gain menace until end of turn."
        ));
    }
}
