package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "20")
public class StoneforgeMystic extends Card {

    public StoneforgeMystic() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)),
                        "Search your library for an Equipment card?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT), "Equipment"),
                        "Put an Equipment card from your hand onto the battlefield?"
                )),
                "{1}{W}, {T}: You may put an Equipment card from your hand onto the battlefield."
        ));
    }
}
