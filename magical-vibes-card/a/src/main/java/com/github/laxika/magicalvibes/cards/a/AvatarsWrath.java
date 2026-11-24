package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;

@CardRegistration(set = "TLA", collectorNumber = "12")
public class AvatarsWrath extends Card {

    public AvatarsWrath() {
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new AirbendTargetPermanentEffect(
                        Subjects.except(Subjects.allCreatures(), Subjects.targetDeclaredElsewhere()),
                        TargetSpec.harmful(TargetPredicates.creature())));
        addSpellEffect(new PlayerRestrictionEffect(Subjects.eachOpponent(),
                PlayerRestriction.castingSpells().fromAnywhereBut(Zone.HAND), Duration.UNTIL_YOUR_NEXT_TURN));
        addSpellEffect(new ExileSpellEffect());
    }
}
