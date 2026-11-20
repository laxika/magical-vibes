package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "175")
public class WilyBandar extends Card {

    public WilyBandar() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())),
                "{2}{G}: Wily Bandar gains indestructible until end of turn."));
    }
}
