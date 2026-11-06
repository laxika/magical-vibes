package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "14")
@CardRegistration(set = "BOT", collectorNumber = "28")
public class SoundwaveSonicSpy extends Card {

    public SoundwaveSonicSpy() {
        setBackFaceCard(new SoundwaveSuperiorCaptain());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{W}{U}{B}"));

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTokenPredicate()))),
                TriggerOptions.ONE_OR_MORE,
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        new CardAllOfPredicate(List.of(instantOrSorcery, new CardManaValueEqualsXPredicate())),
                        GraveyardSearchScope.OPPONENT_GRAVEYARD,
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SoundwaveSuperiorCaptain";
    }
}
