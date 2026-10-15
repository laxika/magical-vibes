package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfDestroyedPermanents;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "37")
@CardRegistration(set = "DSC", collectorNumber = "64")
public class ConvertToSlime extends Card {

    public ConvertToSlime() {
        setAllowSharedTargets(true);
        target(TargetFilters.artifact(), 0, 1);
        target(TargetFilters.creature(), 0, 1);
        target(TargetFilters.enchantment(), 0, 1);
        addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new Delirium(),
                        new CreateTokenEffect("Ooze", new TotalManaValueOfDestroyedPermanents(),
                                new TotalManaValueOfDestroyedPermanents(), CardColor.GREEN,
                                List.of(CardSubtype.OOZE), Set.of(), Set.of())));
    }
}
