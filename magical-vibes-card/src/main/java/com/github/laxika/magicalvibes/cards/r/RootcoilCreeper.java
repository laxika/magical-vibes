package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "238")
public class RootcoilCreeper extends Card {

    public RootcoilCreeper() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(2, ManaSpendRestriction.GRAVEYARD_SPELL_ONLY)),
                "{T}: Add two mana of any one color. Spend this mana only to cast spells from your graveyard."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{U}",
                List.of(
                        new ExileSelfCost(),
                        new ReturnTargetCardFromExileToHandEffect(new CardHasFlashbackPredicate(), true)
                ),
                "{G}{U}, {T}, Exile this creature: Return target card with flashback you own from exile to your hand."
        ));
    }
}
