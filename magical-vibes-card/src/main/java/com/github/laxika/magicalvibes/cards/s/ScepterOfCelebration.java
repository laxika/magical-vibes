package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "64")
@CardRegistration(set = "NCC", collectorNumber = "164")
public class ScepterOfCelebration extends Card {

    public ScepterOfCelebration() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenEffect(CardType.CREATURE, new EventValue(), "Citizen", 1, 1,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.CITIZEN), Set.of(), Set.of(), false, false, Map.of(), List.of(),
                        false, false, false, 0, Set.of()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
