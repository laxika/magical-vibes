package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "660")
public class HYDRADisintegrator extends Card {

    public HYDRADisintegrator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(
                        new CreateTokenEffect(1, "Villain", 2, 1, CardColor.BLACK,
                                List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of()),
                        new AttachMatchingEquipmentToCreatedPermanentEffect(
                                new PermanentIsSourcePermanentPredicate())));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
