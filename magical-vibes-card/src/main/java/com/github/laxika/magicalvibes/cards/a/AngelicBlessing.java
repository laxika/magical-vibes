package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;

import java.util.List;

public class AngelicBlessing extends Card {

    public AngelicBlessing() {
        super("Angelic Blessing", CardType.SORCERY, "{2}{W}", CardColor.WHITE);

        setCardText("Target creature gets +3/+3 and gains flying until end of turn.");
        setNeedsTarget(true);
        setSpellEffects(List.of(
                new BoostTargetCreatureEffect(3, 3),
                new GrantKeywordToTargetEffect(Keyword.FLYING)
        ));
    }
}
