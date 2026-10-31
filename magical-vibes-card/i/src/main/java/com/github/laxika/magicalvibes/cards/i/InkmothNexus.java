package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "145")
@CardRegistration(set = "SLD", collectorNumber = "45")
@CardRegistration(set = "SLD", collectorNumber = "1207")
@CardRegistration(set = "EOS", collectorNumber = "20")
@CardRegistration(set = "EOS", collectorNumber = "65")
@CardRegistration(set = "EOS", collectorNumber = "110")
@CardRegistration(set = "EOS", collectorNumber = "155")
public class InkmothNexus extends Card {

    public InkmothNexus() {
        // {1}: Inkmoth Nexus becomes a 1/1 Phyrexian Blinkmoth artifact creature with flying and infect until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AnimatePermanentsEffect(1, 1,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.BLINKMOTH),
                        Set.of(Keyword.FLYING, Keyword.INFECT),
                        null,
                        Set.of(CardType.ARTIFACT))),
                "{1}: Inkmoth Nexus becomes a 1/1 Phyrexian Blinkmoth artifact creature with flying and infect until end of turn. It's still a land."
        ));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
