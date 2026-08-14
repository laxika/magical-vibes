package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "140")
public class NeurokStealthsuit extends Card {

    public NeurokStealthsuit() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{U}",
                List.of(new AttachSourceEquipmentToTargetCreatureEffect()),
                "{U}{U}: Attach Neurok Stealthsuit to target creature you control.",
                TargetFilters.creatureYouControl()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
