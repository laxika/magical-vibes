package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.IfLostClashEffect;
import com.github.laxika.magicalvibes.model.effect.IfWonClashEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "188")
public class RebellionOfTheFlamekin extends Card {

    public RebellionOfTheFlamekin() {
        // Whenever you clash, you may pay {1}. If you do, create a 3/1 red Elemental Shaman creature
        // token. If you won, that token gains haste until end of turn.
        //
        // The token is created either way, so both branches offer the same "pay {1}" may-pay; only
        // the granted haste differs. Since the clash winner is fixed the moment the trigger fires,
        // TriggerCollectionService.fireClashTriggers picks exactly one branch: the IfWonClashEffect
        // (haste) variant on a win, the IfLostClashEffect (no haste) variant otherwise. Non-targeting,
        // so it goes straight onto the stack rather than the ClashTriggerTarget interaction.
        addEffect(EffectSlot.ON_CONTROLLER_CLASHES, new IfWonClashEffect(
                new MayPayManaEffect("{1}", createTokenEffect(Set.of(Keyword.HASTE)),
                        "Pay {1} to create a 3/1 red Elemental Shaman with haste?")));
        addEffect(EffectSlot.ON_CONTROLLER_CLASHES, new IfLostClashEffect(
                new MayPayManaEffect("{1}", createTokenEffect(Set.of()),
                        "Pay {1} to create a 3/1 red Elemental Shaman?")));
    }

    private static CreateTokenEffect createTokenEffect(Set<Keyword> grantedKeywordsUntilEndOfTurn) {
        return new CreateTokenEffect(1, "Elemental Shaman", 3, 1, CardColor.RED, null,
                List.of(CardSubtype.ELEMENTAL, CardSubtype.SHAMAN), Set.of(), grantedKeywordsUntilEndOfTurn);
    }
}
