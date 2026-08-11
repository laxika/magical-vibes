package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "119")
public class ScarbladesMalice extends Card {

    public ScarbladesMalice() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                        Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new ResolveEffectOnTargetDeathThisTurnEffect(
                        new CreateTokenEffect("Elf", 2, 2, CardColor.BLACK,
                                Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.ELF))));
    }
}
