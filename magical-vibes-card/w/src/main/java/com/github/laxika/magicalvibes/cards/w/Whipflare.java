package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "NPH", collectorNumber = "102")
@CardRegistration(set = "C14", collectorNumber = "184")
@CardRegistration(set = "C16", collectorNumber = "140")
@CardRegistration(set = "CM2", collectorNumber = "130")
public class Whipflare extends Card {

    public Whipflare() {
        addSpellEffect(DealDamageEffect.toEachCreature(2,
                new PermanentNotPredicate(new PermanentIsArtifactPredicate())));
    }
}
