package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
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
        addEffect(EffectSlot.SPELL, new SacrificePermanentThenEffect(
                nonDemonCreature,
                new CreateTokenEffect(
                        1,
                        "Vampire Demon",
                        4,
                        3,
                        CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
                        Set.of(Keyword.FLYING),
                        Set.of()),
                "a non-Demon creature",
                false,
                false));
    }
}
