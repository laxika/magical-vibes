package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CantBecomeTappedUnlessAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "594")
public class OodSphere extends Card {

    public OodSphere() {
        addEffect(EffectSlot.STATIC, GrantSpellCastingAbilityToSpellsEffect.allPlayers(
                Keyword.CONVOKE, new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));

        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        targetUpTo(new Sum(new PlayersInGame(), new Fixed(-1)),
                TargetFilters.creatureAnOpponentControls(), 99)
                .addEffect(EffectSlot.CHAOS_TRIGGERED, new GoadTargetCreatureUntilNextTurnEffect())
                .addEffect(EffectSlot.CHAOS_TRIGGERED, new CantBecomeTappedUnlessAttackingEffect());
    }
}
