package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "INV", collectorNumber = "135")
public class YawgmothsAgenda extends Card {

    public YawgmothsAgenda() {
        addStaticEffect(new LimitSpellsPerTurnEffect(1, SpellLimitScope.CONTROLLER));
        addStaticEffect(new PlayLandsFromGraveyardEffect());
        addStaticEffect(new CastSpellsFromGraveyardEffect(new CardTruePredicate()));
        addStaticEffect(new ExileOwnCardsInsteadOfGraveyardEffect());
    }
}
