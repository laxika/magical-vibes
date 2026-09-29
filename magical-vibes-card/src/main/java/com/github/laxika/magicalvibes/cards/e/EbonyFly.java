package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "60")
public class EbonyFly extends Card {

    public EbonyFly() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        List<CardEffect> animationBranches = List.of(
                animationBranch(1),
                animationBranch(2),
                animationBranch(3),
                animationBranch(4),
                animationBranch(5),
                animationBranch(6));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new RollD6Effect(animationBranches)),
                "{4}: Roll a d6. Until end of turn, you may have this artifact become an X/X Insect artifact creature with flying, where X is the result."
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another attacking creature"
        )).addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }

    private MayEffect animationBranch(int result) {
        return new MayEffect(
                new AnimatePermanentsEffect(
                        result, result, List.of(CardSubtype.INSECT), Set.of(Keyword.FLYING)),
                "Have this artifact become a " + result + "/" + result
                        + " Insect artifact creature with flying?");
    }
}
