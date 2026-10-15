package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

public class KaldringTheRimestaff extends Card {

    public KaldringTheRimestaff() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AllowCastTargetCardFromGraveyardThisTurnEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardSupertypePredicate(CardSupertype.SNOW))),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false,
                        true)),
                "{T}: You may play target snow permanent card from your graveyard this turn. If you do, it enters tapped."
        ));
    }
}
