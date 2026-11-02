package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class BarracksOfTheThousand extends Card {

    public BarracksOfTheThousand() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE)
                        .withProducingSourceForSpellCastTriggers()),
                "{T}: Add {W}."));

        CardAnyOfPredicate artifactOrCreatureSpell = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)));
        addTrigger(Triggers.youCastSpell(),
                SpellCastTriggerEffect.usingManaProducedBySource(artifactOrCreatureSpell,
                        List.of(ThousandMoonsSmithy.gnomeToken())));
    }
}
