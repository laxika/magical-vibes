package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "119")
public class TheWitchsVanity extends Card {

    private static final PermanentPredicate CHAPTER_I_TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
            new PermanentMaxManaValuePredicate(2)));

    public TheWitchsVanity() {
        addTrigger(Triggers.chapter(1), new DestroyTargetPermanentEffect(CHAPTER_I_TARGET));
        setSagaChapterTargetFilter(1, Set.of(new PermanentPredicateTargetFilter(
                CHAPTER_I_TARGET,
                "Target must be a creature an opponent controls with mana value 2 or less")));

        addTrigger(Triggers.chapter(2), foodToken());

        addTrigger(Triggers.chapter(3),
                new CreateTokenAttachedToTargetEffect(wickedRoleToken()));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }

    private static CreateTokenEffect wickedRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Wicked",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                List.of(
                        StaticAbility.of(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE)),
                        new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
