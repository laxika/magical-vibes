package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "M13", collectorNumber = "221")
@CardRegistration(set = "EOS", collectorNumber = "6")
@CardRegistration(set = "EOS", collectorNumber = "51")
@CardRegistration(set = "EOS", collectorNumber = "96")
@CardRegistration(set = "EOS", collectorNumber = "141")
public class CathedralOfWar extends Card {

    public CathedralOfWar() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                ConditionalEffect.atTriggerTime(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
