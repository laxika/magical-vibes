package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "55")
@CardRegistration(set = "FEM", collectorNumber = "113")
public class GoblinFlotilla extends Card {

    public GoblinFlotilla() {
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{R}"),
                        List.of(
                                new GrantEffectToSourceUntilEndOfCombatEffect(
                                        EffectSlot.ON_BLOCK,
                                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                                new GrantEffectToSourceUntilEndOfCombatEffect(
                                        EffectSlot.ON_BECOMES_BLOCKED,
                                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET))),
                        true));
    }
}
