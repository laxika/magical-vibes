package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "4")
@CardRegistration(set = "NCC", collectorNumber = "104")
@CardRegistration(set = "NCC", collectorNumber = "189")
public class KittKantoMayhemDiva extends Card {

    public KittKantoMayhemDiva() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)));

        PermanentPredicate activePlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByActivePlayerPredicate()));
        target(new PermanentPredicateTargetFilter(
                activePlayerCreature,
                "Target must be a creature that player controls"))
                .addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                        new MayPayTapPermanentsEffect(
                                new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                                SequenceEffect.of(
                                        new BoostTargetCreatureEffect(2, 2, activePlayerCreature),
                                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                                        new GoadTargetCreatureUntilNextTurnEffect(activePlayerCreature)),
                                "Tap two untapped creatures you control?"));
    }
}
