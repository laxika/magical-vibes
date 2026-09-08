package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "133")
public class DwarvenHammer extends Card {

    public DwarvenHammer() {
        CreateTokenEffect dwarfBerserker = new CreateTokenEffect(
                "Dwarf Berserker", 2, 1, CardColor.RED,
                List.of(CardSubtype.DWARF, CardSubtype.BERSERKER), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayPayManaEffect("{2}",
                        SequenceEffect.of(dwarfBerserker,
                                new AttachMatchingEquipmentToCreatedPermanentEffect(
                                        new PermanentIsSourcePermanentPredicate())),
                        "Pay {2} to create a Dwarf Berserker token and attach Dwarven Hammer to it?"));
        addEffect(EffectSlot.STATIC,
                new AttachedBoostEffect(new Fixed(3), new Fixed(0), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
