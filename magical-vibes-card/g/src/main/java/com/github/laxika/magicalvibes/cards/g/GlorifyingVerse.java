package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "19")
public class GlorifyingVerse extends Card {

    public GlorifyingVerse() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until
        // end of turn. Each Verse contributes its own trigger, as the printed keyword is not
        // auto-triggered; AttacksAlone is checked at resolution.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // Repartee — Whenever you cast an instant or sorcery spell that targets a creature,
        // conjure a card named Glorifying Verse into your hand.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())),
                new ConjureCardToHandEffect("YSOS", "19"));
    }
}
