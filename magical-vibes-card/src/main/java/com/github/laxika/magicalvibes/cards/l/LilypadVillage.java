package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "255")
public class LilypadVillage extends Card {

    public LilypadVillage() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.BLUE, 1, new ManaRestriction.CreatureSpells())),
                "{T}: Add {U}. Spend this mana only to cast a creature spell."));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new SurveilEffect(2)),
                "{U}, {T}: Surveil 2."
        ).withActivationCondition(
                new PermanentEnteredThisTurn(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.BIRD),
                        new CardSubtypePredicate(CardSubtype.FROG),
                        new CardSubtypePredicate(CardSubtype.OTTER),
                        new CardSubtypePredicate(CardSubtype.RAT)
                )), 1),
                "Activate only if a Bird, Frog, Otter, or Rat entered the battlefield under your control this turn"
        ));
    }
}
