package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasAtLeastAttachedEquipment;
import com.github.laxika.magicalvibes.model.effect.AttachAllEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "16")
@CardRegistration(set = "CMM", collectorNumber = "458")
public class BalanWanderingKnight extends Card {

    public BalanWanderingKnight() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceHasAtLeastAttachedEquipment(2),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new AttachAllEquipmentToSourceEffect(true)),
                "{1}{W}: Attach all Equipment you control to Balan."));
    }
}
