package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualIncorporationEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMKM", collectorNumber = "1")
public class ScrutinyOfTheGuildpact extends Card {

    public ScrutinyOfTheGuildpact() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ChooseCardFromHandAndApplyPerpetualIncorporationEffect(
                        new CardTypePredicate(CardType.CREATURE), "{W}", detectiveToken()),
                "Choose a creature card in your hand to perpetually incorporate {W}?"));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentIsMulticoloredPredicate()));
    }

    private static CreateTokenEffect detectiveToken() {
        return new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE));
    }
}
