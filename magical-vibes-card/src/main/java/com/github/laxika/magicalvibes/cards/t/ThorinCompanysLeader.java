package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "100")
public class ThorinCompanysLeader extends Card {

    public ThorinCompanysLeader() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DWARF),
                        CreateTokenEffect.ofTreasureToken(2)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{10}",
                List.of(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.ALL_OWN_CREATURES)),
                "{10}: Creatures you control gain double strike until end of turn."
        ));
    }
}
