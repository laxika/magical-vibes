package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "STX", collectorNumber = "96")
public class DraconicIntervention extends Card {

    public DraconicIntervention() {
        addCastingCost(ExileCardFromGraveyardCost.trackingExiledManaValue(CardType.INSTANT, CardType.SORCERY));
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)))
                .exilingDamagedCreaturesThatWouldDie());
        addSpellEffect(new ExileSpellEffect());
    }
}
