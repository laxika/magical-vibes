package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReturnTargetCreatureWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "131")
public class RelentlessDead extends Card {

    public RelentlessDead() {
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{B}",
                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                "Pay {B} to return Relentless Dead to its owner's hand?"));

        addEffect(EffectSlot.ON_DEATH, new PayXManaReturnTargetCreatureWithManaValueXEffect(
                new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new CardNotPredicate(new CardIsSelfPredicate())))));
    }
}
