package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TheGreatSynthesis;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "65")
public class JinGitaxias extends Card {

    public JinGitaxias() {
        setBackFaceCard(new TheGreatSynthesis());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardMinManaValuePredicate(3)
                )),
                List.of(new DrawCardEffect(1))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{3}{U}: Exile Jin-Gitaxias, then return it to the battlefield transformed under its owner's control. Activate only as a sorcery and only if you have seven or more cards in hand.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withMinCardsInHand(7));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheGreatSynthesis";
    }
}
