package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "9")
public class BattleMenu extends Card {

    public BattleMenu() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Attack — Create a 2/2 white Knight creature token",
                        new CreateTokenEffect("Knight", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.KNIGHT), Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Ability — Target creature gets +0/+4 until end of turn",
                        new BoostTargetCreatureEffect(0, 4),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Magic — Destroy target creature with power 4 or greater",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(4))),
                                "Target must be a creature with power 4 or greater.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Item — You gain 4 life",
                        new GainLifeEffect(4))
        )));
    }
}
