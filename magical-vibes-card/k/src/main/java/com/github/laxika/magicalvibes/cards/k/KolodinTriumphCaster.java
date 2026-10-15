package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "210")
public class KolodinTriumphCaster extends Card {

    public KolodinTriumphCaster() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MOUNT, CardSubtype.VEHICLE))));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.MOUNT),
                        new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)));

        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.VEHICLE),
                        new AnimatePermanentsEffect(
                                null, null, List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                                GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null)));
    }
}
