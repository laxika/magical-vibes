package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "167")
public class ElvishWarmaster extends Card {

    public ElvishWarmaster() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        new OncePerTurnTriggerEffect(new CreateTokenEffect(
                                "Elf Warrior", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()))));

        PermanentHasSubtypePredicate elves = new PermanentHasSubtypePredicate(CardSubtype.ELF);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(
                        new BoostAllOwnCreaturesEffect(2, 2, elves),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES, elves)
                ),
                "{5}{G}{G}: Elves you control get +2/+2 and gain deathtouch until end of turn."
        ));
    }
}
