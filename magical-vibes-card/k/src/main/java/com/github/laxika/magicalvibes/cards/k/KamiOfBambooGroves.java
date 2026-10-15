package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "24")
public class KamiOfBambooGroves extends Card {

    public KamiOfBambooGroves() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                "Put a land card from your hand onto the battlefield tapped?"));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(SequenceEffect.of(
                        new ConjureCardNamedIntoHandEffect("Forest", false),
                        new ConjureCardNamedIntoHandEffect("Forest", false))),
                "Channel — {2}{G}, Discard this card: Conjure two cards named Forest into your hand."
        ));
    }
}
