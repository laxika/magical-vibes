package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "138")
@CardRegistration(set = "FDN", collectorNumber = "204")
public class KrenkoMobBoss extends Card {

    public KrenkoMobBoss() {
        // {T}: Create X 1/1 red Goblin creature tokens, where X is the number of Goblins you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.CONTROLLER),
                        "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of())),
                "{T}: Create X 1/1 red Goblin creature tokens, where X is the number of Goblins you control."
        ));
    }
}
