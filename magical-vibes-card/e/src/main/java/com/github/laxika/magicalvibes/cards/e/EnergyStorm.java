package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromInstantAndSorcerySpellsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "24")
@CardRegistration(set = "ME2", collectorNumber = "11")
public class EnergyStorm extends Card {

    public EnergyStorm() {
        // Cumulative upkeep {1}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        // Prevent all damage that would be dealt by instant and sorcery spells.
        addStaticEffect(new PreventDamageFromInstantAndSorcerySpellsEffect());

        // Creatures with flying don't untap during their controllers' untap steps.
        addStaticEffect(new MatchingPermanentsDoesntUntapEffect(new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
