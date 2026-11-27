package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "265")
public class HiddenStag extends Card {

    public HiddenStag() {
        addTrigger(Triggers.opponentPlaysLand(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                AnimatePermanentsEffect.sourceBecomesCreature(3, 2, List.of(CardSubtype.ELK, CardSubtype.BEAST), Set.of()));
        addTrigger(Triggers.youPlayLand(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsCreature()),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), Subjects.source()));
    }
}
