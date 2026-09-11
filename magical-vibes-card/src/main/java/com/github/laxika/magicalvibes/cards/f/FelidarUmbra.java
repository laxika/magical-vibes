package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PC2", collectorNumber = "6")
public class FelidarUmbra extends Card {

    public FelidarUmbra() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new TotemArmorEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new AttachSourceAuraToTargetCreatureEffect()),
                "{1}{W}: Attach this Aura to target creature you control.",
                TargetFilters.creatureYouControl()
        ));
    }
}
