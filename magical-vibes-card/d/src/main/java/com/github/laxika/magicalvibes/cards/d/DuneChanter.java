package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToOwnLandsAndLandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "31")
@CardRegistration(set = "OTC", collectorNumber = "67")
public class DuneChanter extends Card {

    public DuneChanter() {
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeToOwnLandsAndLandCardsEffect(CardSubtype.DESERT));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.OWN_LANDS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillControllerThenIfMilledEffect(
                        2,
                        new CardTypePredicate(CardType.LAND),
                        new GainLifeEffect(new EventValue()))),
                "{T}: Mill two cards. You gain 1 life for each land card milled this way."));
    }
}
