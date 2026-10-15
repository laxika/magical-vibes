package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import java.util.List;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "52")
public class ContainmentMembrane extends Card {

    public ContainmentMembrane() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{U}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), false));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
