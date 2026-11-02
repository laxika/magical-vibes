package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "185")
public class ElfhameSanctuary extends Card {

    public ElfhameSanctuary() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                        new SkipDrawStepThisTurnEffect()),
                "Search your library for a basic land card?"));
    }
}
