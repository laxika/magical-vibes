package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "225")
@CardRegistration(set = "M21", collectorNumber = "381")
public class SanctumOfAll extends Card {

    public SanctumOfAll() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(
                        new SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
                                new CardSubtypePredicate(CardSubtype.SHRINE)),
                        "Search your library and/or graveyard for a Shrine card and put it onto the battlefield?"));

        addStaticEffect(new AdditionalTriggeredAbilityEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE),
                        new ControlsPermanentCount(6, new PermanentHasSubtypePredicate(CardSubtype.SHRINE))));
    }
}
