package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "260")
public class HiddenAncients extends Card {

    public HiddenAncients() {
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                AnimatePermanentsEffect.sourceBecomesCreature(5, 5, List.of(CardSubtype.TREEFOLK), Set.of()));
    }
}
