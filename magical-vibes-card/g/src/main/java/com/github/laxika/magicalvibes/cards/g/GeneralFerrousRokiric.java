package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "198")
public class GeneralFerrousRokiric extends Card {

    public GeneralFerrousRokiric() {
        removeKeyword(Keyword.HEXPROOF);
        addStaticEffect(TargetingRestrictionEffect.hexproofFromMonocolored());
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()),
                new CreateTokenEffect(
                        CardType.CREATURE, 1, "Golem", 4, 4,
                        CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                        List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT),
                        false, false, List.of(), List.of(), false, false, false, 0, Set.of()));
    }
}
