package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "230")
public class ArlinnKord extends Card {

    public ArlinnKord() {
        ArlinnEmbracedByTheMoon backFace = new ArlinnEmbracedByTheMoon();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // +1: Until end of turn, up to one target creature gets +2/+2 and gains vigilance and haste.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new BoostTargetCreatureEffect(2, 2),
                        new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.HASTE), GrantScope.TARGET)
                ),
                "+1: Until end of turn, up to one target creature gets +2/+2 and gains vigilance and haste.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                ),
                +1, null, null,
                List.of(), 0, 1
        ));

        // 0: Create a 2/2 green Wolf creature token. Transform Arlinn Kord.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new CreateTokenEffect("Wolf", 2, 2,
                                CardColor.GREEN, List.of(CardSubtype.WOLF),
                                Set.of(), Set.of()),
                        new TransformSelfEffect()
                ),
                "0: Create a 2/2 green Wolf creature token. Transform Arlinn Kord."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "ArlinnEmbracedByTheMoon";
    }
}
