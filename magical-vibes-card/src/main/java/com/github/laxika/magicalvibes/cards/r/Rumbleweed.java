package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "32")
@CardRegistration(set = "OTC", collectorNumber = "68")
public class Rumbleweed extends Card {

    public Rumbleweed() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.LAND), CountScope.CONTROLLER)));

        PermanentPredicate otherCreature = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(3, 3, otherCreature));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Set.of(Keyword.TRAMPLE), GrantScope.OWN_CREATURES, otherCreature));
    }
}
