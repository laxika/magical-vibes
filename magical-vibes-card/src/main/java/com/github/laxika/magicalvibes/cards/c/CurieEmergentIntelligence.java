package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "30")
@CardRegistration(set = "PIP", collectorNumber = "374")
@CardRegistration(set = "PIP", collectorNumber = "558")
@CardRegistration(set = "PIP", collectorNumber = "902")
public class CurieEmergentIntelligence extends Card {

    public CurieEmergentIntelligence() {
        // Whenever Curie deals combat damage to a player, draw cards equal to its base power.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DrawCardEffect(new SourcePower(false, true)));

        PermanentAllOfPredicate anotherNontokenArtifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new ExilePermanentCost(anotherNontokenArtifactCreature,
                                "another nontoken artifact creature", true),
                        new BecomeCopyOfExiledCreaturePermanentlyEffect(
                                new DrawCardEffect(new SourcePower(false, true)))
                ),
                "{1}{U}, Exile another nontoken artifact creature you control: Curie becomes a copy of the exiled creature, "
                        + "except it has \"Whenever this creature deals combat damage to a player, draw cards equal to its base power.\""
        ));
    }
}
