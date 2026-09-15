package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "STA", collectorNumber = "43")
@CardRegistration(set = "RVR", collectorNumber = "118")
public class MizzixsMastery extends Card {

    public MizzixsMastery() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{R}{R}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(instantOrSorcery, ownGraveyard),
                new ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopiesEffect()));
        target(new GraveyardCardPredicateTargetFilter(instantOrSorcery, ownGraveyard));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
