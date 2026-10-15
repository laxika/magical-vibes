package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "294")
public class BasaltGolem extends Card {

    public BasaltGolem() {
        // This creature can't be blocked by artifact creatures.
        addEffect(EffectSlot.STATIC,
                new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentIsArtifactPredicate()));

        // Whenever this creature becomes blocked by a creature, that creature's controller sacrifices
        // it at end of combat. If the player does, they create a 0/2 colorless Wall artifact creature
        // token with defender.
        CreateTokenEffect wallToken = new CreateTokenEffect("Wall", 0, 2, null,
                List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of(CardType.ARTIFACT));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new SacrificeCombatOpponentAtEndOfCombatEffect(wallToken),
                TriggerMode.PER_BLOCKER);
    }
}
