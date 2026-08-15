package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "214")
@CardRegistration(set = "ULG", collectorNumber = "130")
public class QuicksilverAmulet extends Card {

    public QuicksilverAmulet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                        "Put a creature card from your hand onto the battlefield?"
                )),
                "{4}, {T}: You may put a creature card from your hand onto the battlefield."
        ));
    }
}
