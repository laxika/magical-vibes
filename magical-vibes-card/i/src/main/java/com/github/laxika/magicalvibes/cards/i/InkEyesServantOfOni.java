package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "71")
@CardRegistration(set = "PC2", collectorNumber = "33")
@CardRegistration(set = "PCA", collectorNumber = "33")
@CardRegistration(set = "V13", collectorNumber = "13")
@CardRegistration(set = "SLD", collectorNumber = "33")
@CardRegistration(set = "SLD", collectorNumber = "2374")
@CardRegistration(set = "BLC", collectorNumber = "77")
public class InkEyesServantOfOni extends Card {

    public InkEyesServantOfOni() {
        addNinjutsu("{3}{B}{B}");

        // The target is chosen as the trigger goes on the stack; "you may" is asked on resolution.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                        false, new CardTypePredicate(CardType.CREATURE), false),
                        "Put target creature card from that player's graveyard onto the battlefield under your control?"));

        addActivatedAbility(new ActivatedAbility(false, "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Ink-Eyes, Servant of Oni."));
    }
}
