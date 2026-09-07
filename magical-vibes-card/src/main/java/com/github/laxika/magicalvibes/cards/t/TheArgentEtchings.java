package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TheArgentEtchings extends Card {

    public TheArgentEtchings() {
        var incubatorTransform = new ActivatedAbility(
                false,
                "{2}",
                List.of(new TransformSelfEffect()),
                "{2}: Transform this token."
        );
        var incubator = new CreateTokenEffect(
                CardType.ARTIFACT, 5, "Incubator", 0, 0, null, null,
                List.of(), Set.of(), Set.of(), false, false, Map.of(), List.of(incubatorTransform),
                false, false, false, 2, Set.of()
        );
        var incubatorTokens = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(),
                new PermanentNamedPredicate("Incubator")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_I, SequenceEffect.of(
                incubator,
                new TransformAllEffect(incubatorTokens)
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_II, SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(1, 1),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES)
        ));

        var otherPermanents = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentNotPredicate(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsLandPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.PHYREXIAN)
                )))
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_III, SequenceEffect.of(
                new DestroyAllPermanentsEffect(otherPermanents),
                new FlickerEffect(FlickerScope.SELF, null, ReturnTiming.IMMEDIATE, TurnStep.END_STEP,
                        false, null, null, 0, false, false)
        ));
    }
}
