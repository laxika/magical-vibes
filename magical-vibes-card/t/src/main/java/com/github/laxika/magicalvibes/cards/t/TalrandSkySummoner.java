package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "72")
@CardRegistration(set = "DDS", collectorNumber = "11")
@CardRegistration(set = "UMA", collectorNumber = "76")
@CardRegistration(set = "2X2", collectorNumber = "64")
@CardRegistration(set = "TSR", collectorNumber = "318")
@CardRegistration(set = "C15", collectorNumber = "109")
@CardRegistration(set = "CMM", collectorNumber = "124")
@CardRegistration(set = "CMM", collectorNumber = "499")
@CardRegistration(set = "CMM", collectorNumber = "673")
@CardRegistration(set = "C21", collectorNumber = "131")
@CardRegistration(set = "OTC", collectorNumber = "116")
@CardRegistration(set = "C20", collectorNumber = "125")
@CardRegistration(set = "C19", collectorNumber = "97")
public class TalrandSkySummoner extends Card {

    public TalrandSkySummoner() {
        // Whenever you cast an instant or sorcery spell, create a 2/2 blue Drake creature token with flying.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                List.of(new CreateTokenEffect("Drake", 2, 2,
                        CardColor.BLUE,
                        List.of(CardSubtype.DRAKE),
                        Set.of(Keyword.FLYING),
                        Set.of()))
        ));
    }
}
