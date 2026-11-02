package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageByCreaturesExceptEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "21")
@CardRegistration(set = "OPCA", collectorNumber = "42")
public class Kessig extends Card {

    public Kessig() {
        addStaticEffect(new PreventAllCombatDamageByCreaturesExceptEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF)));

        addTrigger(Triggers.chaosEnsues(), new BoostAllOwnCreaturesEffect(2, 2));
        addTrigger(Triggers.chaosEnsues(),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.chaosEnsues(),
                new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.WEREWOLF, GrantScope.OWN_CREATURES));
    }
}
