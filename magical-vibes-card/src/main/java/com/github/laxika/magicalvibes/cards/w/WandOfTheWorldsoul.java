package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantConvokeToNextSpellThisTurnEffect;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "20")
@CardRegistration(set = "MOC", collectorNumber = "107")
public class WandOfTheWorldsoul extends Card {

    public WandOfTheWorldsoul() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantConvokeToNextSpellThisTurnEffect()),
                "{T}: The next spell you cast this turn has convoke."
        ));
    }
}
