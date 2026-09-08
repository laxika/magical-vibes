package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetCardFromGraveyardIfNoSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "163")
public class ConduitOfWorlds extends Card {

    public ConduitOfWorlds() {
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());

        CardAllOfPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CastTargetCardFromGraveyardIfNoSpellThisTurnEffect(
                        nonlandPermanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                "Cast target nonland permanent card from your graveyard if you haven't cast a spell this turn.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
