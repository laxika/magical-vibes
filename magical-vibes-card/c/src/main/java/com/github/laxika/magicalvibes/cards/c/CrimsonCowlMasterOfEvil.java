package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "653")
public class CrimsonCowlMasterOfEvil extends Card {

    public CrimsonCowlMasterOfEvil() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(1, new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.VILLAIN),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())))),
                        new CreateTokenEffect(
                                "Villain", 2, 1, CardColor.BLACK, List.of(CardSubtype.VILLAIN),
                                Set.of(Keyword.MENACE), Set.of())));
    }
}
