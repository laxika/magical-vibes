package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "195")
@CardRegistration(set = "AKR", collectorNumber = "228")
public class AvenWindGuide extends Card {

    public AvenWindGuide() {
        // Flying, vigilance are auto-loaded keywords; no engine wiring needed here.

        // Creature tokens you control have flying and vigilance.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE),
                GrantScope.OWN_CREATURES, new PermanentIsTokenPredicate()));

        // Embalm {4}{W}{U} ({4}{W}{U}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Bird Warrior with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{4}{W}{U}", "Bird Warrior");
    }
}
