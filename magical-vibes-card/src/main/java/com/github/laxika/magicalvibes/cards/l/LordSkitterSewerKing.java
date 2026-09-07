package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "97")
public class LordSkitterSewerKing extends Card {

    public LordSkitterSewerKing() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.RAT),
                        ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard()));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new CreateTokenEffect(1, "Rat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.RAT), Set.of(), Set.of(),
                        Map.of(EffectSlot.STATIC, new CantBlockEffect())));
    }
}
