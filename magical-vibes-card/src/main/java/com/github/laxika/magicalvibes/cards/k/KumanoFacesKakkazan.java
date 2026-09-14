package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EtchingOfKumano;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "152")
public class KumanoFacesKakkazan extends Card {

    public KumanoFacesKakkazan() {
        setBackFaceCard(new EtchingOfKumano());

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToEachMatchingPermanentEffect(
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                EachPermanentScope.ALL_PLAYERS));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new EmpowerNextCreatureSpellThisTurnEffect(false, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "EtchingOfKumano";
    }
}
