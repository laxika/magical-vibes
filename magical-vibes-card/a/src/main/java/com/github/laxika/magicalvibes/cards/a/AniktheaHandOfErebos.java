package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "705")
@CardRegistration(set = "CMM", collectorNumber = "773")
@CardRegistration(set = "CMM", collectorNumber = "780")
public class AniktheaHandOfErebos extends Card {

    public AniktheaHandOfErebos() {
        CardPredicate nonAuraEnchantment = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardNotPredicate(new CardIsAuraPredicate())));

        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE,
                Subjects.otherCreaturesYouControl(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()))));

        var graveyardTarget = target(new GraveyardCardPredicateTargetFilter(
                nonAuraEnchantment,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);
        SequenceEffect copyEffect = SequenceEffect.of(
                ExileGraveyardCardsEffect.upToOneTargetFromControllerGraveyard(nonAuraEnchantment),
                CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                        new CreateTokenCopyOfTargetPermanentEffect(List.of(CardSubtype.ZOMBIE),
                                Set.of(CardType.CREATURE), 3, 3, Map.of(), false, false, false, false, false, false,
                                CardColor.BLACK, Set.of())));
        graveyardTarget.addTrigger(Triggers.enters(ObjectRelation.THIS), copyEffect);
        graveyardTarget.addTrigger(Triggers.attacks(), copyEffect);
    }
}
