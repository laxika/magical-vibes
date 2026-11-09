package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "67")
@CardRegistration(set = "SLC", collectorNumber = "2020")
@CardRegistration(set = "DSC", collectorNumber = "127")
@CardRegistration(set = "OTC", collectorNumber = "113")
public class SharkTyphoon extends Card {

    public SharkTyphoon() {
        CreateTokenEffect castShark = new CreateTokenEffect(
                "Shark", new TargetSpellManaValue(), new TargetSpellManaValue(), CardColor.BLUE,
                List.of(CardSubtype.SHARK), Set.of(Keyword.FLYING), Set.of());
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new CreateTokenForTriggeringPlayerEffect(castShark));

        CreateTokenEffect cyclingShark = new CreateTokenEffect(
                "Shark", new XValue(), new XValue(), CardColor.BLUE,
                List.of(CardSubtype.SHARK), Set.of(Keyword.FLYING), Set.of());
        addTrigger(Triggers.youCycleThisCard(), cyclingShark);
        addCycling("{X}{1}{U}");
    }
}
