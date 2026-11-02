package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "77")
@CardRegistration(set = "FDN", collectorNumber = "326")
@CardRegistration(set = "FDN", collectorNumber = "389")
@CardRegistration(set = "FDN", collectorNumber = "464")
@CardRegistration(set = "FDC", collectorNumber = "135")
public class ZulAshurLichLord extends Card {

    public ZulAshurLichLord() {
        // Ward—Pay 2 life.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(0, 2));

        // {T}: You may cast target Zombie creature card from your graveyard this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AllowCastTargetCardFromGraveyardThisTurnEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.ZOMBIE))),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false)),
                "{T}: You may cast target Zombie creature card from your graveyard this turn."
        ));
    }
}
