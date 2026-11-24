package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CastSpellFromGraveyardOncePerYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardMilledThisTurnPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "115")
@CardRegistration(set = "PIP", collectorNumber = "643")
public class RaulTroubleShooter extends Card {

    public RaulTroubleShooter() {
        addStaticEffect(new CastSpellFromGraveyardOncePerYourTurnEffect(new CardMilledThisTurnPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(1, Subjects.eachPlayer())),
                "{T}: Each player mills a card."));
    }
}
