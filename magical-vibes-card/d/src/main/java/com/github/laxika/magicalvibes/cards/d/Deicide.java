package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "7")
public class Deicide extends Card {

    public Deicide() {
        target(TargetFilters.enchantment());
        addSpellEffect(new ExileTargetPermanentEffect(new PermanentIsEnchantmentPredicate()));
        addSpellEffect(new ConditionalEffect(
                HappenedThisWay.toACard(StepKind.EXILE, new CardSubtypePredicate(CardSubtype.GOD)),
                ExileCardsWithSameNameEffect.anyNumber(Subjects.cardsThisWay(StepKind.EXILE)).searching(
                        Subjects.controllerOf(Subjects.target()), Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY)));
    }
}
