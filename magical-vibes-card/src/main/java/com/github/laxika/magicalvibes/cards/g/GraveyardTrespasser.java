package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MID", collectorNumber = "104")
public class GraveyardTrespasser extends Card {

    public GraveyardTrespasser() {
        setBackFaceCard(new GraveyardGlutton());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exileFromGraveyard(1, false));
        addEffect(EffectSlot.ON_ATTACK, exileFromGraveyard(1, false));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "GraveyardGlutton";
    }

    private ExileCardsFromGraveyardEffect exileFromGraveyard(int maxTargets,
                                                               boolean lifePerCreatureCard) {
        return new ExileCardsFromGraveyardEffect(
                maxTargets,
                new CardTypePredicate(CardType.CREATURE),
                1,
                1,
                false,
                lifePerCreatureCard
        );
    }
}
