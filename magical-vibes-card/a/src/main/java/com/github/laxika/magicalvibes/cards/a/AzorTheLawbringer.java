package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellTypesNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeAndDrawXCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "154")
public class AzorTheLawbringer extends Card {

    public AzorTheLawbringer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new OpponentsCantCastSpellTypesNextTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
        addEffect(EffectSlot.ON_ATTACK, new PayXManaGainXLifeAndDrawXCardsEffect("{X}{W}{U}{U}"));
    }
}
