package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.TriggeringCardMatches;

@CardRegistration(set = "DMC", collectorNumber = "3")
@CardRegistration(set = "DMC", collectorNumber = "78")
public class JensonCarthalionDruidExile extends Card {

    public JensonCarthalionDruidExile() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsMulticoloredPredicate()),
                new ScryEffect(1),
                new ConditionalEffect(new TriggeringCardMatches(
                        new CardHasExactlyNColorsPredicate(5)),
                        new CreateTokenEffect(
                                1, "Angel", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.ANGEL),
                                Set.of(Keyword.FLYING, Keyword.VIGILANCE),
                                Set.of())));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new AwardManaEffect(ManaColor.WHITE),
                        new AwardManaEffect(ManaColor.BLUE),
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED),
                        new AwardManaEffect(ManaColor.GREEN)),
                "{5}, {T}: Add {W}{U}{B}{R}{G}."
        ));
    }
}
