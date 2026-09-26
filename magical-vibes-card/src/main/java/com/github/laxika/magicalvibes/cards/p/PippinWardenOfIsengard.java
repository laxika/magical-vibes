package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "65")
@CardRegistration(set = "LTC", collectorNumber = "146")
public class PippinWardenOfIsengard extends Card {

    private static final String PARTNER_NAME = "Merry, Warden of Isengard";

    public PippinWardenOfIsengard() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put " + PARTNER_NAME + " into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(CreateTokenEffect.ofFoodToken(1)),
                "{1}, {T}: Create a Food token."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(4,
                                new PermanentHasSubtypePredicate(CardSubtype.FOOD)),
                        new BoostAllOwnCreaturesEffect(3, 3, otherCreatures),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES, otherCreatures)
                ),
                "{T}, Sacrifice four Foods: Other creatures you control get +3/+3 and gain haste until end of turn. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
