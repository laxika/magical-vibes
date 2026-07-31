package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAsEntersOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "139")
public class KjeldoranOutpost extends Card {

    public KjeldoranOutpost() {
        // If Kjeldoran Outpost would enter, sacrifice a Plains instead. If you do, put this land onto
        // the battlefield. If you don't, put it into its owner's graveyard.
        addEffect(EffectSlot.STATIC, new SacrificePermanentAsEntersOrGraveyardEffect(
                new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
                "a Plains"));

        // {T}: Add {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {W}."
        ));

        // {1}{W}, {T}: Create a 1/1 white Soldier creature token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(CreateTokenEffect.whiteSoldier(1)),
                "{1}{W}, {T}: Create a 1/1 white Soldier creature token."
        ));
    }
}
