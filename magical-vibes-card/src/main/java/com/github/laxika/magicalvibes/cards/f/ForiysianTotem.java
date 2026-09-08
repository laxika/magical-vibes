package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "254")
public class ForiysianTotem extends Card {

    public ForiysianTotem() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new AnimatePermanentsEffect(
                        4, 4, List.of(CardSubtype.GIANT), Set.of(Keyword.TRAMPLE), CardColor.RED)),
                "{4}{R}: This artifact becomes a 4/4 red Giant artifact creature with trample until end of turn."
        ));
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsSourceCardPredicate()))));
    }
}
