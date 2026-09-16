package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLC", collectorNumber = "21")
@CardRegistration(set = "SLC", collectorNumber = "48")
public class UrzasSaga extends Card {

    public UrzasSaga() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.COLORLESS),
                GrantScope.SELF, null, EffectDuration.WHILE_SOURCE_REMAINS
        ));

        PermanentCount artifactsYouControl = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new CreateTokenEffect(
                                1,
                                "Construct",
                                0,
                                0,
                                null,
                                List.of(CardSubtype.CONSTRUCT),
                                Set.of(),
                                Set.of(CardType.ARTIFACT),
                                Map.of(EffectSlot.STATIC,
                                        new BoostSelfEffect(artifactsYouControl, artifactsYouControl))
                        )),
                        "{2}, {T}: Create a 0/0 colorless Construct artifact creature token with "
                                + "\"This token gets +1/+1 for each artifact you control.\""
                ),
                GrantScope.SELF, null, EffectDuration.WHILE_SOURCE_REMAINS
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardMaxManaValuePredicate(1)
                )),
                LibrarySearchDestination.BATTLEFIELD
        ));
    }
}
