package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;

@CardRegistration(set = "SOS", collectorNumber = "174")
public class AzizaMageTowerCaptain extends Card {

    public AzizaMageTowerCaptain() {
        // Whenever you cast an instant or sorcery spell, you may tap three untapped creatures you control.
        // If you do, copy that spell. You may choose new targets for the copy.
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new MayPayTapPermanentsEffect(
                        new TapMultiplePermanentsCost(3, new PermanentIsCreaturePredicate()),
                        new CopyTriggeringSpellEffect(),
                        "Tap three untapped creatures you control to copy that spell?"));
    }
}
