package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "223")
public class JunglePatrol extends Card {

    public JunglePatrol() {
        // {1}{G}, {T}: Create a 0/1 green Wall creature token with defender named Wood.
        addActivatedAbility(new ActivatedAbility(true, "{1}{G}",
                List.of(new CreateTokenEffect("Wood", 0, 1, CardColor.GREEN,
                        List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.<CardType>of())),
                "{1}{G}, {T}: Create a 0/1 green Wall creature token with defender named Wood."));

        // Sacrifice a token named Wood: Add {R}.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsTokenPredicate(),
                                        new PermanentNamedPredicate("Wood"))),
                                "Sacrifice a token named Wood", false),
                        new AwardManaEffect(ManaColor.RED)),
                "Sacrifice a token named Wood: Add {R}."));
    }
}
