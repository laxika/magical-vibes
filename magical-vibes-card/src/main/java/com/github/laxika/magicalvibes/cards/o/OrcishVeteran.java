package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "62a")
@CardRegistration(set = "FEM", collectorNumber = "62b")
@CardRegistration(set = "FEM", collectorNumber = "62c")
@CardRegistration(set = "FEM", collectorNumber = "127")
@CardRegistration(set = "FEM", collectorNumber = "128")
@CardRegistration(set = "FEM", collectorNumber = "130")
@CardRegistration(set = "FEM", collectorNumber = "131")
public class OrcishVeteran extends Card {

    public OrcishVeteran() {
        PermanentAllOfPredicate whiteCreatureWithPowerAtLeastTwo = new PermanentAllOfPredicate(List.of(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                new PermanentPowerAtLeastPredicate(2)
        ));
        addEffect(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentNotPredicate(whiteCreatureWithPowerAtLeastTwo),
                "creatures that aren't white creatures with power 2 or greater"
        ));
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{R}: This creature gains first strike until end of turn."));
    }
}
