package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "131")
public class RocksteadyCrashCourser extends Card {

    public RocksteadyCrashCourser() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
        addEffect(EffectSlot.STATIC, new EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(
                1, new PermanentHasSubtypePredicate(CardSubtype.BOAR)));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST))),
                "Forestcycling {2} ({2}, Discard this card: Search your library for a Forest card, "
                        + "reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
