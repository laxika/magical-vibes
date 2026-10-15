package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.OpponentsControllingReturnedPermanents;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "311")
@CardRegistration(set = "WOE", collectorNumber = "371")
public class FaerieSlumberParty extends Card {

    public FaerieSlumberParty() {
        addEffect(EffectSlot.SPELL,
                ReturnToHandEffect.allPermanentsMatching(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new Scaled(new OpponentsControllingReturnedPermanents(), 2),
                "Faerie",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FAERIE),
                Set.of(Keyword.FLYING),
                Set.of()
        ).withTokenEffects(Map.of(EffectSlot.STATIC, new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        "creatures with flying"
                )))
        );
    }
}
