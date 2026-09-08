package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "99")
public class MistformWall extends Card {

    public MistformWall() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DEFENDER, GrantScope.SELF, new PermanentHasSubtypePredicate(CardSubtype.WALL)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SourceBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
    }
}
