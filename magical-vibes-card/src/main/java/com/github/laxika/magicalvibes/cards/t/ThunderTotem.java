package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "265")
public class ThunderTotem extends Card {

    public ThunderTotem() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{W}",
                List.of(new AnimatePermanentsEffect(
                        2, 2, List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE), CardColor.WHITE)),
                "{1}{W}{W}: This artifact becomes a 2/2 white Spirit artifact creature with flying and first strike until end of turn."
        ));
    }
}
