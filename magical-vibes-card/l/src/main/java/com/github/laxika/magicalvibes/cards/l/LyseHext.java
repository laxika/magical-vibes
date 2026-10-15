package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FIC", collectorNumber = "88")
@CardRegistration(set = "FIC", collectorNumber = "178")
public class LyseHext extends Card {

    public LyseHext() {
        var noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                noncreatureSpell, 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerCastTwoOrMoreSpellsThisTurn(noncreatureSpell),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)));
    }
}
