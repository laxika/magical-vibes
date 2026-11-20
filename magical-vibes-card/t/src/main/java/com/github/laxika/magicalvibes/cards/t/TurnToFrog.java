package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "78")
@CardRegistration(set = "M15", collectorNumber = "81")
@CardRegistration(set = "ORI", collectorNumber = "81")
@CardRegistration(set = "C14", collectorNumber = "130")
public class TurnToFrog extends Card {

    public TurnToFrog() {
        // Until end of turn, target creature loses all abilities and becomes a blue
        // Frog with base power and toughness 1/1.
        target(TargetFilters.creature())
                .addSpellEffect(new LosesAllAbilitiesEffect(Subjects.target(), Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
                .addSpellEffect(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FROG))
                .addSpellEffect(new SetBasePowerToughnessEffect(1, 1));
    }
}
