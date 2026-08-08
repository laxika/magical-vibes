package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "71")
public class InkEyesServantOfOni extends Card {

    public InkEyesServantOfOni() {
        addNinjutsu("{3}{B}{B}");

        // The combat damage trigger narrows the graveyard choice to the damaged player, and the
        // trigger path allows an empty selection, so "you may put target creature card" reads as
        // up-to-one (decline = choose nothing) with no MayEffect wrapper.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        false, new CardTypePredicate(CardType.CREATURE), false));

        addActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Ink-Eyes, Servant of Oni."));
    }
}
