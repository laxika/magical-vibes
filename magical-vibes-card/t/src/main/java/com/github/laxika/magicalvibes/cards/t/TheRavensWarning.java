package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KHM", collectorNumber = "227")
public class TheRavensWarning extends Card {

    public TheRavensWarning() {
        addTrigger(Triggers.chapter(1),
                new CreateTokenEffect(
                        1, "Bird", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()),
                new GainLifeEffect(2));

        addTrigger(Triggers.chapter(2), new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                TriggerOptions.ONE_OR_MORE, new LookAtHandEffect(), new DrawCardEffect(1))));

        addTrigger(Triggers.chapter(3), new SearchOutsideGameForCardToTopOfLibraryEffect());
    }
}
