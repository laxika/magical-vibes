package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AA3", collectorNumber = "13")
public class GutTrueSoulZealot extends Card {

    public GutTrueSoulZealot() {
        // "Another creature or an artifact": an artifact Gut may sacrifice itself.
        var anotherCreatureOrArtifact = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                ))
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(
                        anotherCreatureOrArtifact,
                        new CreateTokenEffect(1, "Skeleton", 4, 1, CardColor.BLACK,
                                List.of(CardSubtype.SKELETON), Set.of(Keyword.MENACE), true, false),
                        "another creature or artifact", false, false),
                "Sacrifice another creature or artifact?"));
    }
}
