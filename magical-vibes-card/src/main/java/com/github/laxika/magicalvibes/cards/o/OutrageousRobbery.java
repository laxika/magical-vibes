package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MKM", collectorNumber = "97")
@CardRegistration(set = "MKM", collectorNumber = "402")
public class OutrageousRobbery extends Card {

    public OutrageousRobbery() {
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(
                true, null, false, false, 0, null, false, false, false, true));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new ExileTopCardsToSourceEffect(
                new XValue(), true, false, LibraryScope.TARGET_OPPONENT, true));
    }
}
