package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "592")
public class NewNewYork extends Card {

    public NewNewYork() {
        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())));

        ActivatedAbility crewOne = new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1");
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new GrantActivatedAbilityEffect(crewOne, GrantScope.OWN_PERMANENTS,
                        noncreatureArtifact, EffectDuration.UNTIL_END_OF_TURN),
                new AnimatePermanentsEffect(
                        new Fixed(3), new Fixed(3), List.of(CardSubtype.VEHICLE),
                        Set.of(Keyword.FLYING, Keyword.HASTE),
                        null, Set.of(CardType.CREATURE), GrantScope.OWN_PERMANENTS,
                        EffectDuration.UNTIL_END_OF_TURN, noncreatureArtifact)));

        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(CreateTokenEffect.ofTreasureToken(1),
                new CreateTokenEffect(
                        1, "Alien", 2, 2, CardColor.WHITE, List.of(CardSubtype.ALIEN), Set.of(), Set.of())));
    }
}
