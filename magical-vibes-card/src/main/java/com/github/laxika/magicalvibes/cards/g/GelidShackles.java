package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "6")
public class GelidShackles extends Card {

    public GelidShackles() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CantBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{S}",
                List.of(new GrantKeywordEffect(
                        Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE, GrantDuration.END_OF_TURN)),
                "{S}: Enchanted creature gains defender until end of turn."));
    }
}
