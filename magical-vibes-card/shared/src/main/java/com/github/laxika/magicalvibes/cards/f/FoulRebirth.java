package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

public class FoulRebirth extends Card {

    public FoulRebirth() {
        PermanentAllOfPredicate nonDemonCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DEMON))));
        addSpellEffect(SequenceEffect.of(
                new SacrificePermanentsEffect(1, nonDemonCreature, Subjects.you()),
                new ConditionalEffect(HappenedThisWay.sacrificed(), new CreateTokenEffect(
                        1,
                        "Vampire Demon",
                        4,
                        3,
                        CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
                        Set.of(Keyword.FLYING),
                        Set.of()))));
    }
}
