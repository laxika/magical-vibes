package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "67")
public class SharkTyphoon extends Card {

    public SharkTyphoon() {
        CreateTokenEffect castShark = new CreateTokenEffect(
                "Shark", new EventValue(), new EventValue(), CardColor.BLUE,
                List.of(CardSubtype.SHARK), Set.of(Keyword.FLYING), Set.of());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new CreateTokenForTriggeringPlayerEffect(castShark))));

        CreateTokenEffect cyclingShark = new CreateTokenEffect(
                "Shark", new XValue(), new XValue(), CardColor.BLUE,
                List.of(CardSubtype.SHARK), Set.of(Keyword.FLYING), Set.of());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{1}{U}",
                List.of(cyclingShark, new DrawCardEffect(1)),
                "Cycling {X}{1}{U} ({X}{1}{U}, Discard this card: Draw a card.)"
        ));
    }
}
