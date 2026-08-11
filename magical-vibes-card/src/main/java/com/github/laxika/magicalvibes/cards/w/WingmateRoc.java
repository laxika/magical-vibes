package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "31")
public class WingmateRoc extends Card {

    public WingmateRoc() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new Raid(),
                new CreateTokenEffect(
                        "Bird", 3, 4, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));

        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER)));
    }
}
