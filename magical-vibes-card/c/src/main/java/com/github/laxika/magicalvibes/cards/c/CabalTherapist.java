package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "80")
public class CabalTherapist extends Card {

    public CabalTherapist() {
        CardPredicate named = new CardHasNameChosenThisResolutionPredicate();
        // At the beginning of your first main phase, you may sacrifice a creature. When you do,
        // choose a nonland card name, then target player reveals their hand and discards all cards
        // with that name.
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                SequenceEffect.of(
                                        ChooseCardNameAtResolutionEffect.excluding(CardType.LAND),
                                        RevealCardsFromHandEffect.hand(Subjects.targetPlayer()),
                                        new DiscardCardsEffect(Subjects.revealedThisWay(named)))))),
                "Sacrifice a creature?"));
    }
}
