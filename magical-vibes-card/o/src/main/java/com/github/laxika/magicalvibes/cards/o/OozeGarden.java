package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCreateSizedTokenEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "143")
public class OozeGarden extends Card {

    public OozeGarden() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SacrificeCreatureCreateSizedTokenEqualToPowerEffect(
                        new CreateTokenEffect("Ooze", 0, 0, CardColor.GREEN, List.of(CardSubtype.OOZE),
                                Set.of(), Set.of()),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.OOZE))
                )),
                "{1}{G}, Sacrifice a non-Ooze creature: Create an X/X green Ooze creature token, "
                        + "where X is the sacrificed creature's power. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
