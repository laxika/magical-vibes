package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GentlemansRise;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "FIC", collectorNumber = "83")
@CardRegistration(set = "FIC", collectorNumber = "173")
public class HildibrandManderville extends Card {

    public HildibrandManderville() {
        setBackFaceCard(new GentlemansRise());
        addCastingOption(new AdventureCast("{2}{B}"));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentIsTokenPredicate()));
        // The "may" is the later cast; the permission itself is granted when the trigger resolves.
        addEffect(EffectSlot.ON_DEATH, new AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "GentlemansRise";
    }
}
