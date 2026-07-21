package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "31")
public class ChampionOfWits extends Card {

    public ChampionOfWits() {
        // When this creature enters, you may draw cards equal to its power. If you do, discard two cards.
        // SourcePower reads the entering permanent's effective power (the Eternalize token is a 4/4 → draws 4).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(new SourcePower()),
                        new DiscardEffect(2, DiscardRecipient.CONTROLLER)
                ),
                "Draw cards equal to this creature's power, then discard two cards?"
        ));

        // Eternalize {5}{U}{U} ({5}{U}{U}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Snake Wizard with no mana cost. Eternalize only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)
                ),
                "Eternalize {5}{U}{U} ({5}{U}{U}, Exile this card from your graveyard: Create a token that's a "
                        + "copy of it, except it's a 4/4 black Zombie Snake Wizard with no mana cost. Eternalize only "
                        + "as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
