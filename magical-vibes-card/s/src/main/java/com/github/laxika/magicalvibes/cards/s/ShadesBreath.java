package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "167")
public class ShadesBreath extends Card {

    public ShadesBreath() {
        addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLACK, GrantScope.OWN_CREATURES));
        addSpellEffect(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.SHADE, GrantScope.OWN_CREATURES));
        addSpellEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{B}",
                        List.of(new BoostSelfEffect(1, 1)),
                        "{B}: This creature gets +1/+1 until end of turn."
                ),
                GrantScope.OWN_CREATURES,
                null,
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
