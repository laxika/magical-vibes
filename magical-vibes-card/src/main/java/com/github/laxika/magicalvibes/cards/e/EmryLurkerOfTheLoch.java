package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "43")
public class EmryLurkerOfTheLoch extends Card {

    public EmryLurkerOfTheLoch() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(4, MillRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{0}",
                List.of(new GrantTargetGraveyardCardCastEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false)),
                "{T}: Choose target artifact card in your graveyard. You may cast that card this turn."));
    }
}
