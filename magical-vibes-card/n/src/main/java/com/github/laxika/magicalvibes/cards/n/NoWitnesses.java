package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMostCreaturesPredicate;

@CardRegistration(set = "MKM", collectorNumber = "27")
@CardRegistration(set = "MKM", collectorNumber = "390")
public class NoWitnesses extends Card {

    public NoWitnesses() {
        addSpellEffect(CreateTokenEffect.ofClueToken(1).forController(
                Subjects.matching(Subjects.eachPlayer(), new PlayerControlsMostCreaturesPredicate())));
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
