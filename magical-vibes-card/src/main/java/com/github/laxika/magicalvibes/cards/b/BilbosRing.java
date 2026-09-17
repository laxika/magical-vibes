package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOC", collectorNumber = "41")
@CardRegistration(set = "HOC", collectorNumber = "81")
public class BilbosRing extends Card {

    public BilbosRing() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.EQUIPPED_CREATURE));

        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));

        PermanentHasSubtypePredicate halfling = new PermanentHasSubtypePredicate(CardSubtype.HALFLING);
        addActivatedAbility(new EquipActivatedAbility(
                "{1}", halfling, "Bilbo's Ring can be attached only to a Halfling"));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
