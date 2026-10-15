package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "61")
public class PrecognitionField extends Card {

    public PrecognitionField() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileTopCardOfOwnLibraryEffect(false)),
                "{3}: Exile the top card of your library."
        ));
    }
}
