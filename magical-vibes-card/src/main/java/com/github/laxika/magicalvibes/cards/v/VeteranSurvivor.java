package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;

@CardRegistration(set = "DSK", collectorNumber = "40")
public class VeteranSurvivor extends Card {

    public VeteranSurvivor() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new SurvivalTriggerEffect(new ConditionalEffect(
                        new SourceIsTapped(),
                        new ExileGraveyardCardsEffect(
                                1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD,
                                null, null, false, true, false, null, false, true))));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceExiledCardsThreshold(3),
                new BoostSelfEffect(3, 3)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceExiledCardsThreshold(3),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
    }
}
