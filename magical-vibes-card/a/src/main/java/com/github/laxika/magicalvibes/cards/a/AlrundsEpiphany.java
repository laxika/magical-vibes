package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "41")
@CardRegistration(set = "KHM", collectorNumber = "295")
public class AlrundsEpiphany extends Card {

    public AlrundsEpiphany() {
        addSpellEffect(new CreateTokenEffect(
                2, "Bird", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
        addSpellEffect(new ControllerExtraTurnEffect(1));
        addSpellEffect(new ExileSpellEffect());
        addCastingOption(new ForetellCast("{4}{U}{U}"));
    }
}
