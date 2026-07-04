package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "20")
public class InformedInkwright extends Card {

    public InformedInkwright() {
        // Repartee — Whenever you cast an instant or sorcery spell that targets a creature,
        // create a 1/1 white and black Inkling creature token with flying.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new CreateTokenEffect(1, "Inkling", 1, 1,
                        CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.INKLING), Set.of(Keyword.FLYING), Set.of())),
                new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())
        ));
    }
}
