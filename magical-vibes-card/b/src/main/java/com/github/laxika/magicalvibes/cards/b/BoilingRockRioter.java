package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "87")
public class BoilingRockRioter extends Card {

    public BoilingRockRioter() {
        addTrigger(Triggers.attacks(), new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ALLY)),
                        new ExileTargetCardFromGraveyardAndImprintOnSourceEffect(
                                null, GraveyardSearchScope.ALL_GRAVEYARDS)),
                "Tap an untapped Ally you control: Exile target card from a graveyard."
        ));

        addTrigger(Triggers.attacks(),
                new MayCastCardExiledWithSourceEffect(
                        null, new CardSubtypePredicate(CardSubtype.ALLY), false, false, null, true));
    }
}
