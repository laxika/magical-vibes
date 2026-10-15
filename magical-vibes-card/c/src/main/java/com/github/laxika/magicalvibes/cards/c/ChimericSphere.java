package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "148")
public class ChimericSphere extends Card {

    public ChimericSphere() {
        // {2}: Until end of turn, this artifact becomes a 2/1 Construct artifact creature with flying.
        // Flying is a separate GrantKeywordEffect so its layer-6 timestamp can lose to (or beat)
        // the second ability's "loses flying" removal (2008-04-01 ruling).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new AnimatePermanentsEffect(2, 1, List.of(CardSubtype.CONSTRUCT), Set.of()),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{2}: Until end of turn, this artifact becomes a 2/1 Construct artifact creature with flying."
        ));

        // {2}: Until end of turn, this artifact becomes a 3/2 Construct artifact creature and loses flying.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new AnimatePermanentsEffect(3, 2, List.of(CardSubtype.CONSTRUCT), Set.of()),
                        new RemoveKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{2}: Until end of turn, this artifact becomes a 3/2 Construct artifact creature and loses flying."
        ));
    }
}
