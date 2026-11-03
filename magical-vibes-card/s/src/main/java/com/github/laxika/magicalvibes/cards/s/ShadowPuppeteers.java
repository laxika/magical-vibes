package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "12")
@CardRegistration(set = "WOC", collectorNumber = "48")
public class ShadowPuppeteers extends Card {

    public ShadowPuppeteers() {
        // Flying, ward {2}.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(2));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(2, "Faerie Rogue", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.FAERIE, CardSubtype.ROGUE),
                        Set.of(Keyword.FLYING), Set.of()));

        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                new MayEffect(
                        SequenceEffect.of(
                                new AnimatePermanentsEffect(4, 4,
                                        List.of(CardSubtype.DRAGON), Set.of()),
                                new GrantColorUntilEndOfTurnEffect(
                                        CardColor.RED, true, GrantScope.SELF)),
                        "Have it become a red Dragon with base power and toughness 4/4 until end of turn?"));
    }
}
