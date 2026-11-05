package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DoubleOtherOwnCreaturesPowerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "163")
public class GodEternalRhonas extends Card {

    public GodEternalRhonas() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DoubleOtherOwnCreaturesPowerEffect(),
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(2),
                "Put God-Eternal Rhonas into its owner's library third from the top?"));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new SelfExiledFromBattlefieldEffect(
                new MayEffect(
                        new PutSourceCardFromExileIntoLibraryNFromTopEffect(2),
                        "Put God-Eternal Rhonas into its owner's library third from the top?")));
    }
}
