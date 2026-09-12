package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "118")
@CardRegistration(set = "VMA", collectorNumber = "206")
public class ElvishAberration extends Card {

    public ElvishAberration() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardManaEffect(ManaColor.GREEN, 3)), "{T}: Add {G}{G}{G}."));

        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST))),
                "Forestcycling {2} ({2}, Discard this card: Search your library for a Forest card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
