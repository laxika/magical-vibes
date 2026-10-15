package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "60")
public class TheCloningOfShredder extends Card {

    public TheCloningOfShredder() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
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
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopyEffect));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopyEffect));
    }
}
