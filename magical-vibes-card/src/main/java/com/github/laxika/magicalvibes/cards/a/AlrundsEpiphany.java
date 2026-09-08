package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "41")
public class AlrundsEpiphany extends Card {

    public AlrundsEpiphany() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Bird", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
        addCastingOption(new ForetellCast("{4}{U}{U}"));
    }
}
