package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceNonHandSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "33")
public class BilboThiefInTheNight extends Card {

    private static final CardAnyOfPredicate ARTIFACT_INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.ARTIFACT),
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));
    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public BilboThiefInTheNight() {
        addEffect(EffectSlot.STATIC, new ReduceNonHandSpellCastCostEffect(1));
        addEffect(EffectSlot.ON_ATTACK, new CastCardFromGraveyardEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        ARTIFACT_INSTANT_OR_SORCERY)),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                INSTANT_OR_SORCERY,
                true));
    }
}
