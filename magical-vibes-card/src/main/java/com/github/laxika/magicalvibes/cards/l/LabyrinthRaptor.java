package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "193")
public class LabyrinthRaptor extends Card {

    public LabyrinthRaptor() {
        PermanentHasKeywordPredicate menace = new PermanentHasKeywordPredicate(Keyword.MENACE);
        PermanentAllOfPredicate blockingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentBlockingSourcePredicate()));

        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(menace,
                        new SacrificePermanentsEffect(1, blockingCreature, SacrificeRecipient.DEFENDING_PLAYER)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0, menace)),
                "{B}{R}: Creatures you control with menace get +1/+0 until end of turn."
        ));
    }
}
