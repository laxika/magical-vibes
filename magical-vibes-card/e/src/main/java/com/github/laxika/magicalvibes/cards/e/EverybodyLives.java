package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantWinGameEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantLoseLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "18")
@CardRegistration(set = "WHO", collectorNumber = "338")
@CardRegistration(set = "WHO", collectorNumber = "623")
@CardRegistration(set = "WHO", collectorNumber = "929")
public class EverybodyLives extends Card {

    public EverybodyLives() {
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE), Subjects.allCreatures()));
        addSpellEffect(GrantControllerKeywordUntilEndOfTurnEffect.eachPlayer(Keyword.HEXPROOF));
        addSpellEffect(new PlayersCantLoseLifeThisTurnEffect());
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilEndOfTurn(new CantLoseGameEffect()));
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilEndOfTurn(new CantWinGameEffect()));
    }
}
