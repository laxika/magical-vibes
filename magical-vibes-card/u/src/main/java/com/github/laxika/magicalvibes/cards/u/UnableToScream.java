package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeTurnedFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "78")
public class UnableToScream extends Card {

    public UnableToScream() {
        target(TargetFilters.creature())
                .addStaticEffect(new LosesAllAbilitiesEffect(Subjects.attachedCreature()))
                .addStaticEffect(new GrantCardTypeEffect(CardType.ARTIFACT, Subjects.attachedCreature()))
                .addStaticEffect(new GrantCardTypeEffect(CardType.CREATURE, Subjects.attachedCreature()))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.TOY, Subjects.attachedCreature()))
                .addStaticEffect(new SetBasePowerToughnessEffect(0, 2, Subjects.attachedCreature()))
                .addStaticEffect(new CantBeTurnedFaceUpEffect(Subjects.attachedCreature()));
    }
}
