package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "219")
public class DragonThroneOfTarkir extends Card {

    public DragonThroneOfTarkir() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(
                                new BoostAllOwnCreaturesEffect(new SourcePower(), new SourcePower(), otherCreatures),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES, otherCreatures)
                        ),
                        "{2}, {T}: Other creatures you control gain trample and get +X/+X until end of turn, where X is this creature's power."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
