package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "83")
@CardRegistration(set = "FDN", collectorNumber = "517")
@CardRegistration(set = "FDC", collectorNumber = "90")
public class CemeteryRecruitment extends Card {

    public CemeteryRecruitment() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
        addSpellEffect(new ConditionalEffect(
                HappenedThisWay.toACard(StepKind.RETURN, new CardSubtypePredicate(CardSubtype.ZOMBIE)),
                new DrawCardEffect(1)));
    }
}
