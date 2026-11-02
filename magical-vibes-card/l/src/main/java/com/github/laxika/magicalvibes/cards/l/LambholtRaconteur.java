package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "167")
@CardRegistration(set = "DBL", collectorNumber = "434")
public class LambholtRaconteur extends Card {

    public LambholtRaconteur() {
        setBackFaceCard(new LambholtRavager());

        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "LambholtRavager";
    }
}
