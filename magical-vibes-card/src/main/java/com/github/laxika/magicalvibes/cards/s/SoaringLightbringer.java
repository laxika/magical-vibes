package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "11")
@CardRegistration(set = "DSC", collectorNumber = "42")
public class SoaringLightbringer extends Card {

    public SoaringLightbringer() {
        var enchantmentCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.OWN_CREATURES, enchantmentCreature));

        CreateTokenEffect glimmerToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Glimmer", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.GLIMMER), Set.of(), Set.of(CardType.ENCHANTMENT),
                true, false, Map.of(), List.of(), false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new CreateTokenForTriggeringPlayerEffect(glimmerToken, true));
    }
}
