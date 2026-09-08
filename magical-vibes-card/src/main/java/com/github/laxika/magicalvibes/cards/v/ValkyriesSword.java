package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "36")
public class ValkyriesSword extends Card {

    public ValkyriesSword() {
        CreateTokenEffect angel = new CreateTokenEffect(
                CardType.CREATURE, 1, "Angel Warrior", 4, 4, CardColor.WHITE, null,
                List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of(), false, false,
                java.util.Map.of(), List.of(), false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayPayManaEffect("{4}{W}",
                        SequenceEffect.of(angel,
                                new AttachMatchingEquipmentToCreatedPermanentEffect(
                                        new PermanentIsSourcePermanentPredicate())),
                        "Pay {4}{W} to create an Angel Warrior token and attach Valkyrie's Sword to it?"));
        addEffect(EffectSlot.STATIC,
                new AttachedBoostEffect(new Fixed(2), new Fixed(1), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
