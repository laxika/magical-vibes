package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "64")
@CardRegistration(set = "NCC", collectorNumber = "164")
public class ScepterOfCelebration extends Card {

    public ScepterOfCelebration() {
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                new CreateTokenEffect(CardType.CREATURE, new EventValue(), "Citizen", 1, 1,
                        CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                        List.of(CardSubtype.CITIZEN), Set.of(), Set.of(), false, false, List.of(), List.of(),
                        false, false, false, 0, Set.of()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
