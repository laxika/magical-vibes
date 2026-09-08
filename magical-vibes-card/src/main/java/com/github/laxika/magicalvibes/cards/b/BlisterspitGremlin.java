package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "108")
public class BlisterspitGremlin extends Card {

    public BlisterspitGremlin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{1}, {T}: This creature deals 1 damage to each opponent."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new UntapPermanentsEffect(TapUntapScope.SELF))));
    }
}
