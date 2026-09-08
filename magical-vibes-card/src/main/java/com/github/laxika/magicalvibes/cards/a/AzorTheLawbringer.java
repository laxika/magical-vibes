package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellTypesNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeAndDrawXCardsEffect;
import java.util.Set;

@CardRegistration(set = "RIX", collectorNumber = "154")
public class AzorTheLawbringer extends Card {

    public AzorTheLawbringer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new OpponentsCantCastSpellTypesNextTurnEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
        addEffect(EffectSlot.ON_ATTACK, new PayXManaGainXLifeAndDrawXCardsEffect("{X}{W}{U}{U}"));
    }
}
