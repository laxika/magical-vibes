package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;

@CardRegistration(set = "M14", collectorNumber = "170")
@CardRegistration(set = "IMA", collectorNumber = "162")
@CardRegistration(set = "2XM", collectorNumber = "166")
public class Enlarge extends Card {

    public Enlarge() {
        addSpellEffect(new BoostTargetCreatureEffect(7, 7));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        addSpellEffect(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED));
    }
}
