package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "15")
@CardRegistration(set = "VOC", collectorNumber = "53")
public class SpectralArcanist extends Card {

    private static final CardPredicate ELIGIBLE_SPELL = new CardAllOfPredicate(List.of(
            new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.INSTANT),
                    new CardTypePredicate(CardType.SORCERY))),
            new CardManaValueAtMostControlledCountPredicate(
                    new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))));

    public SpectralArcanist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CastCardFromGraveyardEffect(
                        ELIGIBLE_SPELL,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        new CardTruePredicate(),
                        false,
                        true));
    }
}
