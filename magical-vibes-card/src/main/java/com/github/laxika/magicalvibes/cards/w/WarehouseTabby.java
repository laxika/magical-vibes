package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "117")
public class WarehouseTabby extends Card {

    public WarehouseTabby() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new CreateTokenEffect(
                        1,
                        "Rat",
                        1,
                        1,
                        CardColor.BLACK,
                        List.of(CardSubtype.RAT),
                        Set.of(),
                        Set.of(),
                        Map.of(EffectSlot.STATIC, new CantBlockEffect())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)),
                "{1}{B}: This creature gains deathtouch until end of turn."));
    }
}
