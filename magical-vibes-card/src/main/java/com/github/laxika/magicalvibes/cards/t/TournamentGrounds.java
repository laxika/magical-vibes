package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "248")
public class TournamentGrounds extends Card {

    public TournamentGrounds() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(AwardAnyColorManaEffect.forSpellSubtypes(
                        1, Set.of(CardSubtype.KNIGHT, CardSubtype.EQUIPMENT),
                        List.of(ManaColor.RED, ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {R}, {W}, or {B}. Spend this mana only to cast a Knight or Equipment spell."
        ));
    }
}
