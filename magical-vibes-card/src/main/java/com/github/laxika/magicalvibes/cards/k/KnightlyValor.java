package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "13")
@CardRegistration(set = "ORI", collectorNumber = "22")
@CardRegistration(set = "M19", collectorNumber = "20")
public class KnightlyValor extends Card {

    public KnightlyValor() {
        // Enchant creature; on entering create a 2/2 white Knight with vigilance,
        // and the enchanted creature gets +2/+2 and has vigilance.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                        "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT),
                        Set.of(Keyword.VIGILANCE), Set.of()))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        2, 2, Set.of(Keyword.VIGILANCE), GrantScope.ENCHANTED_CREATURE));
    }
}
