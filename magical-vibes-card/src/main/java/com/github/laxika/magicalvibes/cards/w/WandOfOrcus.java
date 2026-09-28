package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "28")
public class WandOfOrcus extends Card {

    public WandOfOrcus() {
        addEffect(EffectSlot.ON_ATTACK, deathtouchGrant());
        addEffect(EffectSlot.ON_BLOCK, deathtouchGrant());
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                new CreateTokenEffect(
                        new EventValue(), "Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }

    private static SequenceEffect deathtouchGrant() {
        return SequenceEffect.of(
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.EQUIPPED_CREATURE),
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
    }
}
