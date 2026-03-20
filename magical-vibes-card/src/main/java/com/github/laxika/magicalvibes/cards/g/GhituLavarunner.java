package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "127")
public class GhituLavarunner extends Card {

    public GhituLavarunner() {
        // As long as there are two or more instant and/or sorcery cards in your graveyard,
        // Ghitu Lavarunner gets +1/+0 and has haste.
        addEffect(EffectSlot.STATIC, new ControllerGraveyardCardThresholdConditionalEffect(
                2,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                new StaticBoostEffect(1, 0, Set.of(Keyword.HASTE), GrantScope.SELF)
        ));
    }
}
