package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LightshieldArray;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "22")
public class InvasionOfGobakhan extends Card {

    public InvasionOfGobakhan() {
        setBackFaceCard(new LightshieldArray());

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                ChooseCardsFromTargetHandEffect.exileAndGrantPlayPermission(
                        1, java.util.List.of(com.github.laxika.magicalvibes.model.CardType.LAND), 2));
    }

    @Override
    public String getBackFaceClassName() {
        return "LightshieldArray";
    }
}
