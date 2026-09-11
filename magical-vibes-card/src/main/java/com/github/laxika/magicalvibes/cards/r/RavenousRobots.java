package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "106")
@CardRegistration(set = "TMT", collectorNumber = "271")
public class RavenousRobots extends Card {

    public RavenousRobots() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ARTIFACT),
                        List.of(new CreateTokenEffect("Robot", 1, 1, null,
                                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT)))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES,
                        new PermanentIsTokenPredicate())),
                "{R}, {T}: Creature tokens you control gain haste until end of turn."
        ));
    }
}
