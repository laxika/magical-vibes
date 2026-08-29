package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "228")
public class PrakhataPillarBug extends Card {

    public PrakhataPillarBug() {
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{B}: Prakhata Pillar-Bug gains lifelink until end of turn."));
    }
}
