package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "37")
@CardRegistration(set = "SPG", collectorNumber = "150")
@CardRegistration(set = "FIC", collectorNumber = "261")
@CardRegistration(set = "SOC", collectorNumber = "188")
@CardRegistration(set = "TDC", collectorNumber = "145")
@CardRegistration(set = "OTC", collectorNumber = "90")
public class ArchmageEmeritus extends Card {

    public ArchmageEmeritus() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> drawCard = List.of(new DrawCardEffect(1));

        // Magecraft — Whenever you cast or copy an instant or sorcery spell, draw a card.
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), drawCard.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell(),
                new SpellCopyTriggerEffect(instantOrSorcery, drawCard));
    }
}
