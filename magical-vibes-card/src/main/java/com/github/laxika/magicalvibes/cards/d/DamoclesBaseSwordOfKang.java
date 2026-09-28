package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "42")
@CardRegistration(set = "MSC", collectorNumber = "347")
public class DamoclesBaseSwordOfKang extends Card {

    public DamoclesBaseSwordOfKang() {
        PermanentAllOfPredicate nontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                TargetPlayerChoosesOneEffect.forTargetedPlayer(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Sacrifice a nontoken creature",
                                new SacrificePermanentsEffect(1, nontokenCreature,
                                        SacrificeRecipient.TARGET_PLAYER)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Lose 2 life and draw two cards",
                                SequenceEffect.of(
                                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                                        new DrawCardEffect(2))))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
