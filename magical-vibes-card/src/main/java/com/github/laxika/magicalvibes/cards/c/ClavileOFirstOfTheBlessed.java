package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "2")
@CardRegistration(set = "LCC", collectorNumber = "18")
@CardRegistration(set = "LCC", collectorNumber = "27")
@CardRegistration(set = "LCC", collectorNumber = "122")
public class ClavileOFirstOfTheBlessed extends Card {

    private static final CreateTokenEffect VAMPIRE_DEMON_TOKEN = new CreateTokenEffect(
            1, "Vampire Demon", 4, 3, CardColor.WHITE,
            Set.of(CardColor.WHITE, CardColor.BLACK),
            List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
            Set.of(Keyword.FLYING), Set.of()).withTapped(true);

    private static final SequenceEffect DEATH_TRIGGER = SequenceEffect.of(
            new DrawCardEffect(), VAMPIRE_DEMON_TOKEN);

    public ClavileOFirstOfTheBlessed() {
        var targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DEMON))));

        target(new PermanentPredicateTargetFilter(
                targetPredicate, "Target must be an attacking Vampire that isn't a Demon"))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        SequenceEffect.of(
                                new GrantEffectToTargetEffect(EffectSlot.ON_DEATH, DEATH_TRIGGER,
                                        EffectDuration.PERMANENT, false),
                                new GrantSubtypeToTargetCreatureEffect(CardSubtype.DEMON)));
    }
}
