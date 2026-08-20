package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "139")
public class GoldspanDragon extends Card {

    public GoldspanDragon() {
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, CreateTokenEffect.ofTreasureToken(1));

        ActivatedAbility treasureManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(2)),
                "{T}, Sacrifice this artifact: Add two mana of any one color."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                treasureManaAbility,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.TREASURE)
        ));
    }
}
