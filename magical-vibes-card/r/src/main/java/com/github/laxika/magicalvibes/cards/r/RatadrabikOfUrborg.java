package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "213")
public class RatadrabikOfUrborg extends Card {

    private static final CreateTokenCopyOfTargetPermanentEffect LEGENDARY_DEATH_COPY =
            new CreateTokenCopyOfTargetPermanentEffect(
                    List.of(CardSubtype.ZOMBIE),
                    Set.of(),
                    2,
                    2,
                    Map.of(),
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    null,
                    Set.of(),
                    false,
                    Map.of(),
                    List.of(),
                    false,
                    true,
                    new Fixed(1),
                    false,
                    Set.of(),
                    false,
                    List.of(CardColor.BLACK));

    public RatadrabikOfUrborg() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.VIGILANCE,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CreateTokenCopyOfDyingCreatureEffect(LEGENDARY_DEATH_COPY)));
    }
}
