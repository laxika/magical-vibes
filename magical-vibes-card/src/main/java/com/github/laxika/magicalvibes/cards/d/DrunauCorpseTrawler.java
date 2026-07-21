package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "63")
public class DrunauCorpseTrawler extends Card {

    public DrunauCorpseTrawler() {
        // When this creature enters, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.blackZombie(1));

        // {2}{B}: Target Zombie gains deathtouch until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{B}",
                List.of(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                "{2}{B}: Target Zombie gains deathtouch until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        "Target must be a Zombie")));
    }
}
