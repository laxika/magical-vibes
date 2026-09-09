package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WildIdea;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "223")
public class SanarUnfinishedGeniusWildIdea extends Card {

    public SanarUnfinishedGeniusWildIdea() {
        setBackFaceCard(new WildIdea());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{T}: Create a Treasure token. Activate only if you've cast an instant or sorcery spell this turn."
        ).withActivationCondition(
                new ControllerCastSpellThisTurn(instantOrSorcery),
                "Activate only if you've cast an instant or sorcery spell this turn"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "WildIdea";
    }
}
