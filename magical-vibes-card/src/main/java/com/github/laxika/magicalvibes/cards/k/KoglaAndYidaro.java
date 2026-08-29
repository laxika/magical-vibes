package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "244")
public class KoglaAndYidaro extends Card {

    public KoglaAndYidaro() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Kogla and Yidaro gains trample and haste until end of turn.",
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.SELF)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Kogla and Yidaro fights target creature you don't control.",
                        new SourceFightsTargetCreatureEffect(),
                        TargetFilters.creatureAnOpponentControls()
                )
        )));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{G}",
                List.of(
                        new DestroyTargetPermanentEffect(),
                        new ShuffleSelfFromGraveyardIntoLibraryEffect(),
                        new DrawCardEffect()
                ),
                "{2}{R}{G}, Discard this card: Destroy up to one target artifact or enchantment. "
                        + "Shuffle this card into your library from your graveyard, then draw a card.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                ),
                null,
                null,
                null,
                List.of(),
                0,
                1
        ));
    }
}
