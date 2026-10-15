package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "GS1", collectorNumber = "22")
public class JiangYanggu extends Card {

    private static final PermanentAllOfPredicate MOWU = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNamedPredicate("Mowu")
    ));

    public JiangYanggu() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new BoostTargetCreatureEffect(2, 2)),
                "+1: Target creature gets +2/+2 until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new ConditionalEffect(
                        new NotCondition(new ControlsPermanent(MOWU)),
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Mowu", 3, 3,
                                CardColor.GREEN, null, List.of(CardSubtype.DOG), Set.of(), Set.of(),
                                false, false, Map.of(), List.of(), false, false, true, 0, Set.of())
                )),
                "−1: If you don't control a creature named Mowu, create Mowu, a legendary 3/3 green Dog creature token."
        ));

        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(
                        new BoostTargetCreatureEffect(landsYouControl, landsYouControl),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "−5: Until end of turn, target creature gains trample and gets +X/+X, where X is the number of lands you control.",
                TargetFilters.creature()
        ));
    }
}
