package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "255")
@CardRegistration(set = "KHM", collectorNumber = "371")
public class FacelessHaven extends Card {

    public FacelessHaven() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{S}{S}{S}",
                List.of(new AnimatePermanentsEffect(4, 3, List.of(),
                        Set.of(Keyword.VIGILANCE, Keyword.CHANGELING), null)),
                "{S}{S}{S}: Faceless Haven becomes a 4/3 creature with vigilance and all creature types until end of turn. It's still a land."
        ));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
