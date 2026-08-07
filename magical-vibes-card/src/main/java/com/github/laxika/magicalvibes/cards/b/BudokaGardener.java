package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DokaiWeaverOfLife;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "202")
public class BudokaGardener extends Card {

    public BudokaGardener() {
        setBackFaceCard(new DokaiWeaverOfLife());

        // "{T}: You may put a land card from your hand onto the battlefield. If you control ten or more
        // lands, flip this creature." - the land-count check is a separate step that happens whether or
        // not a land was put onto the battlefield, and counts the land just put down.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?"
                        ),
                        new ConditionalEffect(
                                new ControlsPermanentCount(10, new PermanentIsLandPredicate()),
                                new TransformToBackFaceEffect()
                        )
                ),
                "{T}: You may put a land card from your hand onto the battlefield. If you control ten or "
                        + "more lands, flip Budoka Gardener."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "DokaiWeaverOfLife";
    }
}
