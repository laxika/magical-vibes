package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "154")
public class SozinsComet extends Card {

    public SozinsComet() {
        addSpellEffect(new GrantKeywordEffect(Keyword.FIREBENDING, GrantScope.OWN_CREATURES));
        addSpellEffect(new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.attacks(), new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 5))));
        addCastingOption(new ForetellCast("{2}{R}"));
    }
}
