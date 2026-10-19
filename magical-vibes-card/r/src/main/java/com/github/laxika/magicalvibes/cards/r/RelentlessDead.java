package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReturnTargetCreatureWithManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "131")
@CardRegistration(set = "SLD", collectorNumber = "850")
@CardRegistration(set = "SLD", collectorNumber = "950")
@CardRegistration(set = "SIR", collectorNumber = "130")
public class RelentlessDead extends Card {

    public RelentlessDead() {
        // "Return it" refers to the card object that died, so the payment only matters while that same
        // graveyard incarnation is still there when the ability resolves (CR 400.7).
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{B}",
                new ConditionalEffect(new SourceCardInGraveyard(),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(), false),
                "Pay {B} to return Relentless Dead to its owner's hand?"));

        addEffect(EffectSlot.ON_DEATH, new PayXManaReturnTargetCreatureWithManaValueXEffect(
                new CardAllOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new CardNotPredicate(new CardIsSelfPredicate())))));
    }
}
