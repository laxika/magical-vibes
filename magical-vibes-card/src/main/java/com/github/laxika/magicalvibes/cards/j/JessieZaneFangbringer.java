package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "17")
public class JessieZaneFangbringer extends Card {

    public JessieZaneFangbringer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardIntoTopCardsOfLibraryEffect(JessieZaneFangbringer::conjuredAmbushViper, 6));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardSubtypePredicate(CardSubtype.SNAKE),
                        List.of(new ConjureCardIntoTopCardsOfLibraryEffect(
                                JessieZaneFangbringer::conjuredAmbushViper, 6))));
    }

    private static Card conjuredAmbushViper() {
        Card card = new AmbushViper();
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        return card;
    }
}
