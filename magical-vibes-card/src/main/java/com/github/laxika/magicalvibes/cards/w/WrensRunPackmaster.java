package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "244")
public class WrensRunPackmaster extends Card {

    public WrensRunPackmaster() {
        // Champion an Elf.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect(CardSubtype.ELF));

        // {2}{G}: Create a 2/2 green Wolf creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new CreateTokenEffect("Wolf", 2, 2, CardColor.GREEN,
                        List.of(CardSubtype.WOLF), Set.of(), Set.of())),
                "{2}{G}: Create a 2/2 green Wolf creature token."));

        // Wolves you control have deathtouch.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.WOLF)));
    }
}
