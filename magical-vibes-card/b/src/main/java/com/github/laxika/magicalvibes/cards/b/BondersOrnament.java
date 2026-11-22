package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsPermanentPredicate;

@CardRegistration(set = "CMM", collectorNumber = "370")
@CardRegistration(set = "C20", collectorNumber = "67")
public class BondersOrnament extends Card {

    public BondersOrnament() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new DrawCardEffect(1, Subjects.matching(Subjects.eachPlayer(),
                        new PlayerControlsPermanentPredicate(new PermanentNamedPredicate("Bonder's Ornament"))))),
                "{4}, {T}: Each player who controls a permanent named Bonder's Ornament draws a card."
        ));
    }
}
