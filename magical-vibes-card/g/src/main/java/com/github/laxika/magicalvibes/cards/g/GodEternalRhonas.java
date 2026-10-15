package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DoubleOtherOwnCreaturesPowerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;

@CardRegistration(set = "WAR", collectorNumber = "163")
public class GodEternalRhonas extends Card {

    public GodEternalRhonas() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DoubleOtherOwnCreaturesPowerEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));

        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Rhonas into its owner's library third from the top?"));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Rhonas into its owner's library third from the top?")));
    }
}
