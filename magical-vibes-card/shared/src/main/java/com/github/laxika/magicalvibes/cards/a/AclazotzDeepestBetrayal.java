package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "88")
@CardRegistration(set = "LCI", collectorNumber = "316")
public class AclazotzDeepestBetrayal extends Card {

    public AclazotzDeepestBetrayal() {
        setBackFaceCard(new TempleOfTheDead());

        addTrigger(Triggers.attacks(), new EachOpponentDiscardsOrControllerDrawsEffect());
        addTrigger(Triggers.opponentDiscardsCard()
                .withFilter(new CardTypePredicate(CardType.LAND)),
                new CreateTokenEffect("Bat", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.BAT), Set.of(Keyword.FLYING), Set.of()));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceTransformedFromGraveyardEffect(true, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "TempleOfTheDead";
    }
}
