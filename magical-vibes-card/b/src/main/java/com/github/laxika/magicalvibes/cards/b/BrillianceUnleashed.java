package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetGraveyardCardMatches;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "141")
public class BrillianceUnleashed extends Card {

    public BrillianceUnleashed() {
        CardTypePredicate artifact = new CardTypePredicate(CardType.ARTIFACT);
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        ReturnCardFromGraveyardEffect.ReturnCardFromGraveyardEffectBuilder returnArtifact =
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(artifact)
                        .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                        .targetGraveyard(true);
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Brilliance Unleashed deals 5 damage to target creature",
                        DealDamageEffect.toTargetCreature(5), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target artifact card from your graveyard to the battlefield; if it isn't an artifact creature card, it's a 3/3 Robot artifact creature with flying",
                        List.of(
                                new ConditionalEffect(
                                        new TargetGraveyardCardMatches(creature, "an artifact creature card"),
                                        returnArtifact.build()),
                                new ConditionalEffect(
                                        new TargetGraveyardCardMatches(new CardNotPredicate(creature),
                                                "a noncreature artifact card"),
                                        returnArtifact.battlefieldEffectGrants(List.of(
                                                new GrantCardTypeEffect(CardType.CREATURE, Subjects.target()),
                                                new GrantSubtypeEffect(CardSubtype.ROBOT, Subjects.target()),
                                                SetBasePowerToughnessEffect.indefinitely(3, 3),
                                                new GrantKeywordEffect(Keyword.FLYING, Subjects.target(),
                                                        Duration.PERMANENT))).build())),
                        new GraveyardCardPredicateTargetFilter(artifact,
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        )));
    }
}
