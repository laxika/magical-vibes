package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "190")
public class ReignOfChaos extends Card {

    public ReignOfChaos() {
        setAllowSharedTargets(true);

        TargetFilter plains = new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
                "Target must be a Plains."
        );
        TargetFilter whiteCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE))
                )),
                "Target must be a white creature."
        );
        TargetFilter island = new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.ISLAND),
                "Target must be an Island."
        );
        TargetFilter blueCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE))
                )),
                "Target must be a blue creature."
        );

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target Plains and target white creature",
                        List.<CardEffect>of(new DestroyTargetPermanentEffect(false), new DestroyTargetPermanentEffect(false)),
                        List.of(plains, whiteCreature)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target Island and target blue creature",
                        List.<CardEffect>of(new DestroyTargetPermanentEffect(false), new DestroyTargetPermanentEffect(false)),
                        List.of(island, blueCreature)
                )
        )));
    }
}
