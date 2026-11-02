package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "60")
public class TheCloningOfShredder extends Card {

    public TheCloningOfShredder() {
        addTrigger(Triggers.chapter(1),
                new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        true,
                        List.of(CardSubtype.MUTANT),
                        false,
                        false,
                        true,
                        true));

        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect =
                CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                        List.of(CardSubtype.MUTANT), Set.of(), null, null, Map.of());
        addTrigger(Triggers.chapter(2),
                new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopyEffect));
        addTrigger(Triggers.chapter(3),
                new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopyEffect));
    }
}
