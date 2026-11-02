package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsNoPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "115")
public class SotheraTheSupervoid extends Card {

    public SotheraTheSupervoid() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new EachOpponentChoosesCreatureToExileWithSourceEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(
                        new AnyPlayerControlsNoPermanent(new PermanentIsCreaturePredicate()),
                        SequenceEffect.of(
                                new SacrificeSelfEffect(),
                                new ReturnCardExiledWithSourceToBattlefieldEffect(
                                        new CardTypePredicate(CardType.CREATURE), false, null,
                                        false, false, false, 2))));
    }
}
