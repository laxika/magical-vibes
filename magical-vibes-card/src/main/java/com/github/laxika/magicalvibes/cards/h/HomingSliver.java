package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "118")
public class HomingSliver extends Card {

    public HomingSliver() {
        addCycling("{3}");

        ActivatedAbility slivercycling = new ActivatedAbility(false, "{3}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SLIVER))),
                "Slivercycling {3} ({3}, Discard this card: Search your library for a Sliver card, "
                        + "reveal it, put it into your hand, then shuffle.)");
        addEffect(EffectSlot.STATIC, new GrantHandActivatedAbilityToCardsEffect(
                slivercycling, new CardSubtypePredicate(CardSubtype.SLIVER)));
    }
}
