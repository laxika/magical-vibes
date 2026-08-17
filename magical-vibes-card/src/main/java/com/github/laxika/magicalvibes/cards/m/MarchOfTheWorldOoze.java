package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "169")
public class MarchOfTheWorldOoze extends Card {

    public MarchOfTheWorldOoze() {
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(6, 6, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.OOZE, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                SpellCastTriggerEffect.duringYourTurn(null, List.of(new CreateTokenEffect(
                        "Elephant", 3, 3, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()))));
    }
}
