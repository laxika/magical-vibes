package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "43")
public class SecretIdentity extends Card {

    public SecretIdentity() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Conceal — Target creature you control becomes a Citizen with base power and toughness 1/1 and gains hexproof until end of turn",
                        List.of(
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.CITIZEN),
                                new SetBasePowerToughnessEffect(1, 1),
                                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET)),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Reveal — Target creature you control becomes a Hero with base power and toughness 3/4 and gains flying and vigilance until end of turn",
                        List.of(
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.HERO),
                                new SetBasePowerToughnessEffect(3, 4),
                                new GrantKeywordEffect(
                                        java.util.Set.of(Keyword.FLYING, Keyword.VIGILANCE), GrantScope.TARGET)),
                        TargetFilters.creatureYouControl())
        )));
    }
}
