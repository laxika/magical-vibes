package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "276")
@CardRegistration(set = "V12", collectorNumber = "6")
@CardRegistration(set = "EXP", collectorNumber = "39")
@CardRegistration(set = "2X2", collectorNumber = "323")
@CardRegistration(set = "C16", collectorNumber = "296")
public class ForbiddenOrchard extends Card {

    public ForbiddenOrchard() {
        // {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        // Whenever you tap this land for mana, target opponent creates a 1/1 colorless
        // Spirit creature token.
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.youTapThisLandForMana(), new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Spirit", 1, 1, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of()),
                PlayerRelation.OPPONENT
        ));
    }
}
