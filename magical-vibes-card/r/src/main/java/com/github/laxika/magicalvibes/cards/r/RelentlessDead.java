package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.condition.TargetGraveyardCardManaValueEqualsStepAmount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "131")
@CardRegistration(set = "SLD", collectorNumber = "850")
@CardRegistration(set = "SLD", collectorNumber = "950")
@CardRegistration(set = "SIR", collectorNumber = "130")
public class RelentlessDead extends Card {

    public RelentlessDead() {
        // "Return it" refers to the card object that died, so the payment only matters while that same
        // graveyard incarnation is still there when the ability resolves (CR 400.7).
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayPayManaEffect("{B}",
                new ConditionalEffect(new SourceCardInGraveyard(),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect()),
                "Pay {B} to return Relentless Dead to its owner's hand?"));

        // The target is chosen as the ability goes on the stack, X only as it resolves (CR 118.12).
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(SequenceEffect.of(
                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                        new ConditionalEffect(new TargetGraveyardCardManaValueEqualsStepAmount(StepKind.PAY_MANA),
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardSubtypePredicate(CardSubtype.ZOMBIE),
                                                new CardNotPredicate(new CardIsSelfPredicate()))))
                                        .targetGraveyard(true)
                                        .build())),
                "Pay {X} to return another target Zombie creature card with mana value X?"));
    }
}
