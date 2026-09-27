package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1343")
@CardRegistration(set = "SLD", collectorNumber = "1378")
public class AngelOfTheRuins extends Card {

    public AngelOfTheRuins() {
        PermanentPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));
        target(new PermanentPredicateTargetFilter(
                artifactOrEnchantment,
                "Target must be an artifact or enchantment"), 0, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentEffect(artifactOrEnchantment));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        LibrarySearchDestination.HAND)),
                "Plainscycling {2} ({2}, Discard this card: Search your library for a Plains card, reveal it, "
                        + "put it into your hand, then shuffle.)"
        ));
    }
}
