package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "99")
public class KalitasBloodchiefOfGhet extends Card {

    public KalitasBloodchiefOfGhet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}{B}",
                List.of(new DestroyTargetCreatureThenCreateTokenEqualToPowerToughnessEffect(
                        new CreateTokenEffect("Vampire", 0, 0, CardColor.BLACK,
                                List.of(CardSubtype.VAMPIRE), Set.of(), Set.of()))),
                "{B}{B}{B}, {T}: Destroy target creature. If that creature dies this way, create a black "
                        + "Vampire creature token. Its power is equal to that creature's power and its "
                        + "toughness is equal to that creature's toughness.",
                TargetFilters.creature()
        ));
    }
}
