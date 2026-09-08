package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "83")
public class ProfessorOnyx extends Card {

    public ProfessorOnyx() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> magecraft = List.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(2));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery, magecraft));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(instantOrSorcery, magecraft));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER),
                        LookAtTopCardsEffect.chooseNToHandRestToGraveyard(3, 1)),
                "+1: You lose 1 life. Look at the top three cards of your library. Put one of them "
                        + "into your hand and the rest into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new SacrificePermanentsEffect(1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate())),
                        SacrificeRecipient.EACH_OPPONENT)),
                "\u22123: Each opponent sacrifices a creature with the greatest power among creatures "
                        + "that player controls."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new EachOpponentMayDiscardOrLoseLifeEffect(3),
                        new EachOpponentMayDiscardOrLoseLifeEffect(3),
                        new EachOpponentMayDiscardOrLoseLifeEffect(3),
                        new EachOpponentMayDiscardOrLoseLifeEffect(3),
                        new EachOpponentMayDiscardOrLoseLifeEffect(3),
                        new EachOpponentMayDiscardOrLoseLifeEffect(3),
                        new EachOpponentMayDiscardOrLoseLifeEffect(3)),
                "\u22128: Each opponent may discard a card. If they don't, they lose 3 life. Repeat this "
                        + "process six more times."
        ));
    }
}
