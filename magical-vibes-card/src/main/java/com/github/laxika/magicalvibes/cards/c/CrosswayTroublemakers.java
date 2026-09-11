package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "518")
public class CrosswayTroublemakers extends Card {

    private static final MayPayLifeEffect DEATH_TRIGGER = new MayPayLifeEffect(
            2, new DrawCardEffect(), "Pay 2 life to draw a card?");

    public CrosswayTroublemakers() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)))));

        addEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.VAMPIRE), DEATH_TRIGGER));
    }
}
