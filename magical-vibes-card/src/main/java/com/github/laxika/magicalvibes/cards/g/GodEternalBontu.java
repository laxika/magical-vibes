package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

@CardRegistration(set = "WAR", collectorNumber = "92")
public class GodEternalBontu extends Card {

    public GodEternalBontu() {
        PermanentPredicate otherPermanent = new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(otherPermanent));

        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Bontu into its owner's library third from the top?"));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Bontu into its owner's library third from the top?")));
    }
}
