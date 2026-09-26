package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMoreLifeThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSameTargetAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "88")
@CardRegistration(set = "MSC", collectorNumber = "409")
public class NamorAtlanteanKing extends Card {

    public NamorAtlanteanKing() {
        // Whenever you cast a noncreature spell, create a 1/1 blue Merfolk creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new CreateTokenEffect("Merfolk", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.MERFOLK), Set.of(), Set.of()))
        ));

        // Whenever Namor attacks a player who has more life than you, other creatures you control
        // attacking that player get +2/+0 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new TriggeringPermanentConditionalEffect(
                new PermanentAttacksPlayerWithMoreLifeThanControllerPredicate(),
                new BoostAllOwnCreaturesEffect(2, 0, new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingSameTargetAsSourcePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )))
        ));
    }
}
