package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "132")
public class AllosaurusShepherd extends Card {

    public AllosaurusShepherd() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect(
                new CardColorPredicate(CardColor.GREEN)));

        PermanentHasSubtypePredicate elves = new PermanentHasSubtypePredicate(CardSubtype.ELF);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(
                        new SetAllOwnCreaturesBasePowerToughnessEffect(5, 5, elves),
                        new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.DINOSAUR,
                                GrantScope.OWN_CREATURES, elves)),
                "{4}{G}{G}: Until end of turn, each Elf creature you control has base power and toughness 5/5 and becomes a Dinosaur in addition to its other creature types."
        ));
    }
}
