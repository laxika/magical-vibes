package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "88")
public class AclazotzDeepestBetrayal extends Card {

    public AclazotzDeepestBetrayal() {
        setBackFaceCard(new TempleOfTheDead());

        addEffect(EffectSlot.ON_ATTACK, new EachOpponentDiscardsOrControllerDrawsEffect());
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.LAND),
                new CreateTokenEffect("Bat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of())));
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect(true, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "TempleOfTheDead";
    }
}
