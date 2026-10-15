package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRC", collectorNumber = "2")
@CardRegistration(set = "BRC", collectorNumber = "40")
@CardRegistration(set = "BRC", collectorNumber = "49")
public class UrzaChiefArtificer extends Card {

    public UrzaChiefArtificer() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())), CountScope.CONTROLLER)));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.OWN_CREATURES, new PermanentIsArtifactPredicate()));

        CreateTokenEffect constructToken = new CreateTokenEffect(
                1,
                "Construct",
                0,
                0,
                null,
                List.of(CardSubtype.CONSTRUCT),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER),
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER),
                        GrantScope.SELF)));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, constructToken);
    }
}
