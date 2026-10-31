package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceClassLevelAtLeast;
import com.github.laxika.magicalvibes.model.effect.CastCreatureSpellsWithSneakFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "67")
public class NinjaTeen extends Card {

    public NinjaTeen() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new ClassLevelUpEffect(2)),
                "{1}{B}: Level 2. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceClassLevelAtLeast(2)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new ClassLevelUpEffect(3)),
                "{B}: Level 3. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceClassLevelAtLeast(2),
                        new NotCondition(new SourceClassLevelAtLeast(3)))),
                "This Class must be level 2."));

        addStaticEffect(new ConditionalEffect(
                new SourceClassLevelAtLeast(2),
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES)));
        addStaticEffect(new ConditionalEffect(
                new SourceClassLevelAtLeast(2),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES)));
        addStaticEffect(new ConditionalEffect(
                new SourceClassLevelAtLeast(3),
                new CastCreatureSpellsWithSneakFromGraveyardEffect("{3}{B}")));
    }
}
