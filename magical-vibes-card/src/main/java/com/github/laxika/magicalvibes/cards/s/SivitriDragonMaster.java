package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RequireLifePaymentToAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "43")
@CardRegistration(set = "DMC", collectorNumber = "65")
public class SivitriDragonMaster extends Card {

    public SivitriDragonMaster() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new RequireLifePaymentToAttackControllerUntilNextTurnEffect(2)),
                "+1: Until your next turn, creatures can't attack you or planeswalkers you control unless their controller pays 2 life for each of those creatures."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DRAGON))),
                "−3: Search your library for a Dragon card, reveal it, put it into your hand, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))
                )))),
                "−7: Destroy all non-Dragon creatures."
        ));
    }
}
