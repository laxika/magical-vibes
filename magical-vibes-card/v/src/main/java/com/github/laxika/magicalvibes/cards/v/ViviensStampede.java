package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.PlayersDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;

import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "65")
@CardRegistration(set = "NCC", collectorNumber = "165")
public class ViviensStampede extends Card {

    public ViviensStampede() {
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.MELEE), Subjects.otherCreaturesYouControl()));
        addSpellEffect(RegisterDelayedStepTriggerEffect.atNextMainPhaseThisTurn(ActivePlayer.ANY,
                new DrawCardEffect(new PlayersDealtCombatDamageThisTurn())));
    }
}
