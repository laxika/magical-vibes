package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "55")
public class TestamentOfFaith extends Card {

    public TestamentOfFaith() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new AnimatePermanentsEffect(
                        new XValue(), new XValue(),
                        List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), null, Set.of(),
                        GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null
                )),
                "{X}: This enchantment becomes an X/X Wall creature with defender in addition to its other types until end of turn."
        ));
    }
}
