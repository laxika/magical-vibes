package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "180")
public class JeskaiAscendancy extends Card {

    public JeskaiAscendancy() {
        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                noncreatureSpell,
                List.of(
                        new BoostAllOwnCreaturesEffect(1, 1),
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate())
                )
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        noncreatureSpell,
                        List.of(SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                        ))
                ),
                "Draw a card, then discard a card?"
        ));
    }
}
