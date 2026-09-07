package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "236")
public class VraskaSwarmsEminence extends Card {

    public VraskaSwarmsEminence() {
        CardEffect putCounterOnDealer = new PutCounterOnReferencedPermanentEffect(
                PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE);
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH), putCounterOnDealer));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH), putCounterOnDealer));

        Map<EffectSlot, CardEffect> assassinTokenEffects = Map.of(
                EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourcePermanentPredicate(), new DestroyTargetPermanentEffect()));
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Assassin", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.ASSASSIN), Set.of(Keyword.DEATHTOUCH), Set.of(), assassinTokenEffects)),
                "-2: Create a 1/1 black Assassin creature token with deathtouch and \"Whenever this token deals damage to a planeswalker, destroy that planeswalker.\""));
    }
}
