package com.github.laxika.magicalvibes.cards.g;

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
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "59")
public class GiantsAmulet extends Card {

    public GiantsAmulet() {
        CreateTokenEffect giantWizard = new CreateTokenEffect(
                "Giant Wizard", 4, 4, CardColor.BLUE,
                List.of(CardSubtype.GIANT, CardSubtype.WIZARD), Set.of(), Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayPayManaEffect("{3}{U}",
                        SequenceEffect.of(giantWizard,
                                new AttachMatchingEquipmentToCreatedPermanentEffect(
                                        new PermanentIsSourcePermanentPredicate())),
                        "Pay {3}{U} to create a Giant Wizard token and attach Giant's Amulet to it?"));
        addEffect(EffectSlot.STATIC,
                new AttachedBoostEffect(new Fixed(0), new Fixed(1), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new EnchantedPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsTappedPredicate()),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.EQUIPPED_CREATURE),
                        null));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
