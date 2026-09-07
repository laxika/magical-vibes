package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "43")
@CardRegistration(set = "FIN", collectorNumber = "330")
public class TheWindCrystal extends Card {

    public TheWindCrystal() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardColorPredicate(CardColor.WHITE), 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new DoubleLifeGainEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{W}{W}",
                List.of(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.OWN_CREATURES)),
                "{4}{W}{W}, {T}: Creatures you control gain flying and lifelink until end of turn."
        ));
    }
}
