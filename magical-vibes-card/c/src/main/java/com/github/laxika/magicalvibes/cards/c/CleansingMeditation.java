package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "TOR", collectorNumber = "3")
public class CleansingMeditation extends Card {

    public CleansingMeditation() {
        DestroyTargetPermanentEffect destroyAllEnchantments = DestroyTargetPermanentEffect.all(
                new PermanentIsEnchantmentPredicate());
        addSpellEffect(new ConditionalReplacementEffect(
                new GraveyardCardThreshold(7, null),
                destroyAllEnchantments,
                SequenceEffect.of(
                        destroyAllEnchantments,
                        new PutCardsOntoBattlefieldEffect(
                                Subjects.inYourGraveyard(Subjects.cardsThisWay(StepKind.DESTROY))))));
    }
}
