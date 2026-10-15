package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "142")
public class BaruFistOfKrosa extends Card {

    public BaruFistOfKrosa() {
        PermanentColorInPredicate green = new PermanentColorInPredicate(Set.of(CardColor.GREEN));
        TriggeringCardConditionalEffect forestTrigger = new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.FOREST),
                SequenceEffect.of(
                        new BoostAllOwnCreaturesEffect(1, 1, green),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES, green)));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, forestTrigger);
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, forestTrigger);

        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(
                                new CardNamedPredicate("Baru, Fist of Krosa"),
                                "Baru, Fist of Krosa"),
                        new CreateTokenEffect(
                                "Wurm",
                                landsYouControl,
                                landsYouControl,
                                CardColor.GREEN,
                                List.of(CardSubtype.WURM),
                                Set.of(),
                                Set.of())
                ),
                "Grandeur — Discard another card named Baru, Fist of Krosa: Create an X/X green Wurm creature token, "
                        + "where X is the number of lands you control."
        ));
    }
}
