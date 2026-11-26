package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
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
        addSpellEffect(DestroyTargetPermanentEffect.eachTarget());
        addSpellEffect(new ConditionalEffect(
                        new Delirium(),
                        new CreateTokenEffect("Ooze", AmountThisWay.destroyedManaValue(),
                                AmountThisWay.destroyedManaValue(), CardColor.GREEN,
                                List.of(CardSubtype.OOZE), Set.of(), Set.of())));
    }
}
