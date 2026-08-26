package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "216")
public class LazavWearerOfFaces extends Card {

    public LazavWearerOfFaces() {
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new ExileTargetCardFromGraveyardAndTrackWithSourceEffect(GraveyardSearchScope.ALL_GRAVEYARDS),
                CreateTokenEffect.ofClueToken(1)));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new MayEffect(
                                new BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect(),
                                "Have Lazav become a copy of a creature card exiled with it until end of turn?")));
    }
}
