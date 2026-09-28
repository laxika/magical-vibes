package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "3")
@CardRegistration(set = "MOC", collectorNumber = "91")
@CardRegistration(set = "MOC", collectorNumber = "136")
public class GimbalGremlinProdigy extends Card {

    public GimbalGremlinProdigy() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsArtifactPredicate()));

        PermanentAllOfPredicate artifactToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsTokenPredicate()));
        CreateTokenEffect gremlinToken = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Gremlin",
                0,
                0,
                CardColor.RED,
                Set.of(CardColor.RED),
                List.of(CardSubtype.GREMLIN),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new CreateXTokenWithXCountersEffect(
                        gremlinToken,
                        new DistinctPermanentNamesAmongControlled(artifactToken, CountScope.CONTROLLER),
                        CounterType.PLUS_ONE_PLUS_ONE));
    }
}
