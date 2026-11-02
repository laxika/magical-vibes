package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.ForetoldCardsInExile;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasForetellPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "226")
public class NikoDefiesDestiny extends Card {

    public NikoDefiesDestiny() {
        addTrigger(Triggers.chapter(1),
                new GainLifeEffect(new Scaled(new ForetoldCardsInExile(CountScope.CONTROLLER), 2)));
        addTrigger(Triggers.chapter(2),
                new AwardRestrictedManaEffect(ManaColor.WHITE, 1, new ManaRestriction.ForetellSpells()));
        addTrigger(Triggers.chapter(2),
                new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.ForetellSpells()));
        addTrigger(Triggers.chapter(3), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardHasForetellPredicate())
                .targetGraveyard(true)
                .build());
    }
}
