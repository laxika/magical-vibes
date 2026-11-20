package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTC", collectorNumber = "40")
@CardRegistration(set = "OTC", collectorNumber = "76")
public class CactusPreserve extends Card {

    public CactusPreserve() {
        addStaticEffect(new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfTypeLandsCouldProduceEffect(
                        ManaColorLandScope.CONTROLLER, new PermanentIsLandPredicate())),
                "{T}: Add one mana of any type that a land you control could produce."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AnimatePermanentsEffect(
                        new GreatestManaValueAmongOwnedCommanders(),
                        new GreatestManaValueAmongOwnedCommanders(),
                        List.of(CardSubtype.PLANT), Set.of(Keyword.REACH), null,
                        Set.of(), Subjects.source(), Duration.UNTIL_END_OF_TURN,
                        null, Set.of(CardColor.GREEN))),
                "{3}: Until end of turn, this land becomes an X/X green Plant creature with reach, "
                        + "where X is the greatest mana value among your commanders. It's still a land."
        ));
    }
}
