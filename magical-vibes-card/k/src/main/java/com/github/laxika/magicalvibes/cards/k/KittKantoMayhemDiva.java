package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "4")
@CardRegistration(set = "NCC", collectorNumber = "104")
@CardRegistration(set = "NCC", collectorNumber = "189")
public class KittKantoMayhemDiva extends Card {

    public KittKantoMayhemDiva() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)));

        PermanentPredicate activePlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByActivePlayerPredicate()));
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                UnlessPaysEffect.youMayPay(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                BoostEffect.target(2, 2, activePlayerCreature),
                                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()),
                                new GoadTargetCreatureUntilNextTurnEffect(activePlayerCreature))))
                        .withPrompt("Tap two untapped creatures you control?"));
    }
}
