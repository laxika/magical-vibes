package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "FIC", collectorNumber = "32")
@CardRegistration(set = "FIC", collectorNumber = "110")
public class UltimateMagicHoly extends Card {

    public UltimateMagicHoly() {
        addSpellEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS));
        addSpellEffect(new ConditionalEffect(
                new CastFromZone(Zone.EXILE),
                PreventDamageEffect.allToController()));
        addCastingOption(new ForetellCast("{2}{W}"));
    }
}
