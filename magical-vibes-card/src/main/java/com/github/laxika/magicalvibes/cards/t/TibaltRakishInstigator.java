package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "146")
public class TibaltRakishInstigator extends Card {

    public TibaltRakishInstigator() {
        addEffect(EffectSlot.STATIC, new OpponentsCantGainLifeEffect());

        Map<EffectSlot, CardEffect> devilTokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Devil", 1, 1, CardColor.RED,
                        List.of(CardSubtype.DEVIL), Set.of(), Set.of(), devilTokenEffects)),
                "-2: Create a 1/1 red Devil creature token with \"When this token dies, it deals 1 damage to any target.\""));
    }
}
